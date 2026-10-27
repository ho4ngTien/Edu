package com.edu.identity.application.service;

import com.edu.identity.infrastructure.persistence.entity.RefreshTokenEntity;
import com.edu.identity.infrastructure.persistence.repository.RefreshTokenJpaRepository;
import com.edu.shared.kernel.exception.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenJpaRepository refreshTokenRepository;
    private final Clock clock;
    private final SecureRandom secureRandom = new SecureRandom();

    public RefreshTokenService(RefreshTokenJpaRepository refreshTokenRepository, Clock clock) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.clock = clock;
    }

    public static String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    public String generateRawToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    @Transactional
    public String createRefreshToken(UUID userId, UUID familyId, Duration ttl) {
        String rawToken = generateRawToken();
        String hash = hashToken(rawToken);
        Instant now = clock.instant();

        RefreshTokenEntity entity = new RefreshTokenEntity(
                UUID.randomUUID(),
                userId,
                hash,
                familyId != null ? familyId : UUID.randomUUID(),
                now.plus(ttl),
                now
        );

        refreshTokenRepository.save(entity);
        return rawToken;
    }

    @Transactional
    public RotationResult rotateRefreshToken(String rawRefreshToken, Duration ttl) {
        String hash = hashToken(rawRefreshToken);
        Instant now = clock.instant();

        RefreshTokenEntity currentToken = refreshTokenRepository.findByTokenHashWithLock(hash)
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

        if (currentToken.isUsed()) {
            refreshTokenRepository.revokeFamily(currentToken.getFamilyId(), now);
            throw new BadCredentialsException("Compromised refresh token reused. Session revoked.");
        }

        if (!currentToken.isActive(now)) {
            throw new BadCredentialsException("Refresh token is expired or revoked");
        }

        currentToken.markAsUsed(now);
        refreshTokenRepository.save(currentToken);

        String newRawToken = generateRawToken();
        String newHash = hashToken(newRawToken);

        RefreshTokenEntity newToken = new RefreshTokenEntity(
                UUID.randomUUID(),
                currentToken.getUserId(),
                newHash,
                currentToken.getFamilyId(),
                now.plus(ttl),
                now
        );
        refreshTokenRepository.save(newToken);

        return new RotationResult(currentToken.getUserId(), newRawToken);
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        String hash = hashToken(rawRefreshToken);
        Instant now = clock.instant();
        refreshTokenRepository.findByTokenHash(hash).ifPresent(token -> {
            token.revoke(now);
            refreshTokenRepository.save(token);
        });
    }

    public record RotationResult(UUID userId, String newRefreshToken) {
    }
}
