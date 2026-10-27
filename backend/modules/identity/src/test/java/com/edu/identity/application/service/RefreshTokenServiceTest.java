package com.edu.identity.application.service;

import com.edu.identity.infrastructure.persistence.entity.RefreshTokenEntity;
import com.edu.identity.infrastructure.persistence.repository.RefreshTokenJpaRepository;
import com.edu.shared.kernel.exception.BadCredentialsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenJpaRepository refreshTokenRepository;

    private Clock fixedClock;
    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(Instant.parse("2026-10-27T10:00:00Z"), ZoneOffset.UTC);
        refreshTokenService = new RefreshTokenService(refreshTokenRepository, fixedClock);
    }

    @Test
    @DisplayName("AC-2.6.5.2: Create refresh token generates secure token, hashes with SHA-256 and saves to repository")
    void createRefreshTokenSuccess() {
        UUID userId = UUID.randomUUID();
        String rawToken = refreshTokenService.createRefreshToken(userId, null, Duration.ofDays(7));

        assertThat(rawToken).isNotBlank();

        ArgumentCaptor<RefreshTokenEntity> captor = ArgumentCaptor.forClass(RefreshTokenEntity.class);
        verify(refreshTokenRepository).save(captor.capture());

        RefreshTokenEntity saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.getTokenHash()).isEqualTo(RefreshTokenService.hashToken(rawToken));
        assertThat(saved.getFamilyId()).isNotNull();
        assertThat(saved.getExpiresAt()).isEqualTo(Instant.parse("2026-11-03T10:00:00Z"));
    }

    @Test
    @DisplayName("AC-2.6.5.3: Rotate refresh token marks old token as used and issues new token in same family")
    void rotateRefreshTokenSuccess() {
        UUID userId = UUID.randomUUID();
        UUID familyId = UUID.randomUUID();
        String rawToken = "old-raw-token-123";
        String tokenHash = RefreshTokenService.hashToken(rawToken);

        RefreshTokenEntity current = new RefreshTokenEntity(
                UUID.randomUUID(),
                userId,
                tokenHash,
                familyId,
                Instant.parse("2026-10-28T10:00:00Z"),
                Instant.parse("2026-10-21T10:00:00Z")
        );

        given(refreshTokenRepository.findByTokenHashWithLock(tokenHash)).willReturn(Optional.of(current));

        RefreshTokenService.RotationResult result = refreshTokenService.rotateRefreshToken(rawToken, Duration.ofDays(7));

        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.newRefreshToken()).isNotBlank().isNotEqualTo(rawToken);
        assertThat(current.isUsed()).isTrue();
        assertThat(current.getUsedAt()).isEqualTo(Instant.parse("2026-10-27T10:00:00Z"));
    }

    @Test
    @DisplayName("AC-2.6.5.4: Reuse detection - using an already-used refresh token revokes family and throws exception")
    void reuseDetectionRevokesFamily() {
        UUID userId = UUID.randomUUID();
        UUID familyId = UUID.randomUUID();
        String rawToken = "compromised-token";
        String tokenHash = RefreshTokenService.hashToken(rawToken);

        RefreshTokenEntity current = new RefreshTokenEntity(
                UUID.randomUUID(),
                userId,
                tokenHash,
                familyId,
                Instant.parse("2026-10-28T10:00:00Z"),
                Instant.parse("2026-10-21T10:00:00Z")
        );
        current.markAsUsed(Instant.parse("2026-10-25T10:00:00Z"));

        given(refreshTokenRepository.findByTokenHashWithLock(tokenHash)).willReturn(Optional.of(current));

        assertThatThrownBy(() -> refreshTokenService.rotateRefreshToken(rawToken, Duration.ofDays(7)))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Compromised refresh token reused");

        verify(refreshTokenRepository).revokeFamily(familyId, Instant.parse("2026-10-27T10:00:00Z"));
    }

    @Test
    @DisplayName("AC-2.6.5.6: Logout revokes refresh token")
    void logoutRevokesToken() {
        String rawToken = "logout-token";
        String tokenHash = RefreshTokenService.hashToken(rawToken);

        RefreshTokenEntity current = new RefreshTokenEntity(
                UUID.randomUUID(),
                UUID.randomUUID(),
                tokenHash,
                UUID.randomUUID(),
                Instant.parse("2026-10-28T10:00:00Z"),
                Instant.parse("2026-10-21T10:00:00Z")
        );

        given(refreshTokenRepository.findByTokenHash(tokenHash)).willReturn(Optional.of(current));

        refreshTokenService.logout(rawToken);

        assertThat(current.getRevokedAt()).isEqualTo(Instant.parse("2026-10-27T10:00:00Z"));
        verify(refreshTokenRepository).save(current);
    }
}
