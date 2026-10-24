# Phase 2.6: Checkpoint 2.6.5 — Refresh Token Management & Lifecycle

> Thuộc [Phase 2.6: Security & JWT Token Management](README.md)

## 8. Checkpoint 2.6.5: Refresh Token Management & Lifecycle

Mục tiêu của checkpoint 2.6.5 là quản lý vòng đời của Refresh Token: tạo chuỗi ngẫu nhiên bảo mật cao, băm SHA-256 lưu trữ trong PostgreSQL, luân chuyển token (Rotation), phát hiện tái sử dụng (Reuse Detection) và hủy phiên (Logout).

### 8.1. Flyway Migration: `V5__create_identity_refresh_tokens.sql`

Vị trí: `backend/boot/src/main/resources/db/migration/V5__create_identity_refresh_tokens.sql` (đồng bộ tại `backend/modules/identity/src/main/resources/db/migration/`)

```sql
-- Migration V5: Create table identity.refresh_tokens
-- Schema: identity
-- Purpose: Quản lý vòng đời Opaque Refresh Token, Rotation và Token Family

CREATE TABLE identity.refresh_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    family_id UUID NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    used_at TIMESTAMPTZ NULL,
    revoked_at TIMESTAMPTZ NULL,
    CONSTRAINT chk_refresh_tokens_expires_after_created CHECK (expires_at > created_at)
);

-- Chỉ mục tối ưu hóa tra cứu theo user, family và token hash
CREATE INDEX idx_refresh_tokens_user_id ON identity.refresh_tokens (user_id);
CREATE INDEX idx_refresh_tokens_family_id ON identity.refresh_tokens (family_id);
CREATE INDEX idx_refresh_tokens_token_hash ON identity.refresh_tokens (token_hash);
```

---

### 8.2. JPA Entity & Repository

Vị trí: `backend/modules/identity/src/main/java/com/edu/identity/infrastructure/persistence/entity/RefreshTokenEntity.java`

```java
package com.edu.identity.infrastructure.persistence.entity;

import java.time.Instant;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "refresh_tokens", schema = "identity")
public class RefreshTokenEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "family_id", nullable = false)
    private UUID familyId;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "used_at")
    private Instant usedAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    protected RefreshTokenEntity() {}

    public RefreshTokenEntity(
        UUID id,
        UUID userId,
        String tokenHash,
        UUID familyId,
        Instant expiresAt,
        Instant createdAt
    ) {
        this.id = id;
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.familyId = familyId;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
    }

    public boolean isActive(Instant now) {
        return usedAt == null && revokedAt == null && expiresAt.isAfter(now);
    }

    public boolean isUsed() {
        return usedAt != null;
    }

    public void markAsUsed(Instant now) {
        this.usedAt = now;
    }

    public void revoke(Instant now) {
        this.revokedAt = now;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getTokenHash() { return tokenHash; }
    public UUID getFamilyId() { return familyId; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUsedAt() { return usedAt; }
    public Instant getRevokedAt() { return revokedAt; }
}
```

Vị trí: `backend/modules/identity/src/main/java/com/edu/identity/infrastructure/persistence/repository/RefreshTokenJpaRepository.java`

```java
package com.edu.identity.infrastructure.persistence.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import com.edu.identity.infrastructure.persistence.entity.RefreshTokenEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM RefreshTokenEntity r WHERE r.tokenHash = :tokenHash")
    Optional<RefreshTokenEntity> findByTokenHashWithLock(@Param("tokenHash") String tokenHash);

    Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);

    @Modifying
    @Query("UPDATE RefreshTokenEntity r SET r.revokedAt = :now WHERE r.familyId = :familyId AND r.revokedAt IS NULL")
    void revokeFamily(@Param("familyId") UUID familyId, @Param("now") Instant now);

    @Modifying
    @Query("UPDATE RefreshTokenEntity r SET r.revokedAt = :now WHERE r.userId = :userId AND r.revokedAt IS NULL")
    void revokeAllForUser(@Param("userId") UUID userId, @Param("now") Instant now);
}
```

---

### 8.3. Bộ sinh Token & Băm SHA-256

Vị trí: `backend/modules/identity/src/main/java/com/edu/identity/application/service/RefreshTokenService.java`

```java
package com.edu.identity.application.service;

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
import com.edu.identity.infrastructure.persistence.entity.RefreshTokenEntity;
import com.edu.identity.infrastructure.persistence.repository.RefreshTokenJpaRepository;
import com.edu.shared.kernel.domain.exception.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        // Khóa bi quan chống race condition khi 2 request refresh gửi cùng lúc
        RefreshTokenEntity currentToken = refreshTokenRepository.findByTokenHashWithLock(hash)
            .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

        // Phát hiện tái sử dụng Token (Token Reuse Detection)
        if (currentToken.isUsed()) {
            refreshTokenRepository.revokeFamily(currentToken.getFamilyId(), now);
            throw new BadCredentialsException("Compromised refresh token reused. Session revoked.");
        }

        // Kiểm tra tính hiệu lực
        if (!currentToken.isActive(now)) {
            throw new BadCredentialsException("Refresh token is expired or revoked");
        }

        // Đánh dấu token cũ đã sử dụng
        currentToken.markAsUsed(now);
        refreshTokenRepository.save(currentToken);

        // Sinh token mới trong cùng một token family
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

    public record RotationResult(UUID userId, String newRefreshToken) {}
}
```

---

### 8.4. Endpoints Refresh & Logout

Vị trí: `backend/modules/identity/src/main/java/com/edu/identity/infrastructure/web/TokenController.java`

```java
package com.edu.identity.infrastructure.web;

import java.util.UUID;
import com.edu.identity.api.dto.LoginResponse;
import com.edu.identity.application.port.TokenIssuer;
import com.edu.identity.application.service.RefreshTokenService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class TokenController {

    private final RefreshTokenService refreshTokenService;
    private final TokenIssuer tokenIssuer;

    public TokenController(RefreshTokenService refreshTokenService, TokenIssuer tokenIssuer) {
        this.refreshTokenService = refreshTokenService;
        this.tokenIssuer = tokenIssuer;
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        // Delegate việc cấp mới token qua TokenIssuer adapter
        var rotation = refreshTokenService.rotateRefreshToken(request.refreshToken(), java.time.Duration.ofDays(7));
        var newTokens = tokenIssuer.issue(rotation.userId());

        return ResponseEntity.ok(new LoginResponse(
            newTokens.accessToken(),
            "Bearer",
            newTokens.expiresIn(),
            rotation.newRefreshToken()
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest request) {
        refreshTokenService.logout(request.refreshToken());
        return ResponseEntity.noContent().build();
    }

    public record RefreshTokenRequest(@NotBlank String refreshToken) {}
    public record LogoutRequest(@NotBlank String refreshToken) {}
}
```

---

### 8.5. Tiêu chí Hoàn thành Checkpoint 2.6.5

| ID | Tiêu chí | Trạng thái |
| :--- | :--- | :---: |
| **AC-2.6.5.1** | Flyway migration `V5` chạy thành công, tạo bảng `identity.refresh_tokens` | **DONE** |
| **AC-2.6.5.2** | Sinh Opaque Token ngẫu nhiên 256-bit bằng `SecureRandom`, băm SHA-256 lưu DB | **DONE** |
| **AC-2.6.5.3** | Cơ chế Rotation cấp mới Access + Refresh Token và vô hiệu hóa token cũ | **DONE** |
| **AC-2.6.5.4** | Tái sử dụng token cũ kích hoạt cơ chế thu hồi toàn bộ `family_id` | **DONE** |
| **AC-2.6.5.5** | Khóa bi quan `PESSIMISTIC_WRITE` ngăn ngừa race condition khi refresh đồng thời | **DONE** |
| **AC-2.6.5.6** | Endpoint `POST /api/v1/auth/logout` thu hồi token thành công (204 No Content) | **DONE** |

---

