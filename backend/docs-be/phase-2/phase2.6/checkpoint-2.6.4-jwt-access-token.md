# Phase 2.6: Checkpoint 2.6.4 — JWT Access Token

> Thuộc [Phase 2.6: Security & JWT Token Management](README.md)

## 7. Checkpoint 2.6.4: JWT Access Token

Mục tiêu của checkpoint 2.6.4 là triển khai cơ chế tạo chữ ký số và giải mã/xác thực JWT Access Token theo tiêu chuẩn RFC 7519, tích hợp vào hệ thống Spring Security 6.

### 7.1. Cấu hình Khóa Ký HMAC-SHA256 (SecretKey)

Vị trí: `backend/boot/src/main/java/com/edu/boot/security/JwtSecurityConfig.java`

```java
package com.edu.boot.security;

import java.util.Base64;
import java.util.List;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

@Configuration
public class JwtSecurityConfig {

    private final JwtProperties jwtProperties;

    public JwtSecurityConfig(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @Bean
    public SecretKey jwtSecretKey() {
        byte[] keyBytes = Base64.getDecoder().decode(jwtProperties.secretBase64());
        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT Secret must be at least 256 bits (32 bytes)");
        }
        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    @Bean
    public JwtEncoder jwtEncoder(SecretKey secretKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(secretKey));
    }

    @Bean
    public JwtDecoder jwtDecoder(SecretKey secretKey) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(secretKey)
            .macAlgorithm(MacAlgorithm.HS256)
            .build();

        // Validator kiểm tra Timestamp (exp & nbf)
        OAuth2TokenValidator<Jwt> defaultTimestampValidator = new JwtTimestampValidator();

        // Validator kiểm tra Issuer (iss)
        OAuth2TokenValidator<Jwt> issuerValidator = new JwtClaimValidator<String>(
            JwtClaimNames.ISS,
            iss -> iss != null && iss.equals(jwtProperties.issuer())
        );

        // Validator kiểm tra Audience (aud)
        OAuth2TokenValidator<Jwt> audienceValidator = new JwtClaimValidator<List<String>>(
            JwtClaimNames.AUD,
            aud -> aud != null && aud.contains(jwtProperties.audience())
        );

        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
            defaultTimestampValidator,
            issuerValidator,
            audienceValidator
        ));

        return decoder;
    }
}
```

---

### 7.2. Triển khai Service Phát hành Access Token

Vị trí: `backend/boot/src/main/java/com/edu/boot/security/JwtAccessTokenService.java`

```java
package com.edu.boot.security;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class JwtAccessTokenService {

    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;
    private final Clock clock;

    public JwtAccessTokenService(
        JwtEncoder jwtEncoder,
        JwtProperties jwtProperties,
        Clock clock
    ) {
        this.jwtEncoder = jwtEncoder;
        this.jwtProperties = jwtProperties;
        this.clock = clock;
    }

    public String generateAccessToken(UUID userId) {
        Instant now = clock.instant();
        Instant expiresAt = now.plus(jwtProperties.accessTokenTtl());

        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer(jwtProperties.issuer())
            .audience(List.of(jwtProperties.audience()))
            .subject(userId.toString())
            .issuedAt(now)
            .expiresAt(expiresAt)
            .id(UUID.randomUUID().toString())
            .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
```

---

### 7.3. Tiêu chí Hoàn thành Checkpoint 2.6.4

| ID | Tiêu chí | Trạng thái |
| :--- | :--- | :---: |
| **AC-2.6.4.1** | JWT Encoder tạo token hợp chuẩn HS256 với đầy đủ header và claims | **DONE** |
| **AC-2.6.4.2** | Khóa bí mật dưới 32 bytes kích hoạt ngoại lệ `IllegalStateException` ngay khi khởi động | **DONE** |
| **AC-2.6.4.3** | JWT Decoder kiểm tra thành công chữ ký số và thời gian hết hạn (`exp`) | **DONE** |
| **AC-2.6.4.4** | Token có `iss` hoặc `aud` không khớp bị từ chối với lỗi 401 | **DONE** |
| **AC-2.6.4.5** | Inject bean `java.time.Clock` chuẩn UTC từ `shared-kernel`, hỗ trợ mock trong test | **DONE** |

---

