# Phase 2.6: Checkpoint 2.6.2 — Security Dependencies & Configuration

> Thuộc [Phase 2.6: Security & JWT Token Management](README.md)

## 5. Checkpoint 2.6.2: Security Dependencies & Configuration

Mục tiêu của checkpoint 2.6.2 là khai báo đúng các thư viện bảo mật trong Maven Multi-Module, thiết lập file cấu hình thuộc tính theo môi trường và chuẩn bị bộ khung Security Configuration.

### 5.1. Khai báo Dependencies trong Maven

Theo thiết kế Modular Monolith của E-Edu:
- Module `boot` chịu trách nhiệm chạy ứng dụng Web và cấu hình Security Filter Chain. Do đó, các starter bảo mật được đặt tại `boot/pom.xml`.
- Module `identity` đã có `spring-security-crypto` (đã sử dụng từ Phase 2.5 cho `PasswordEncoderConfig`).

Bổ sung vào `backend/boot/pom.xml`:

```xml
<!-- Spring Security Starter -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- OAuth2 Resource Server & Nimbus JOSE for JWT Processing -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>

<!-- Security Test Slice -->
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-test</artifactId>
    <scope>test</scope>
</dependency>
```

> [!NOTE]
> Không khai báo `<version>` thủ công vì Spring Boot 3.4.3 Parent POM (`com.edu:parent`) đã quản lý phiên bản tương thích thông qua Spring Boot Dependencies BOM.

---

### 5.2. Cấu hình Thuộc tính Bảo mật (Configuration Properties)

Khai báo các thuộc tính bảo mật trong `backend/boot/src/main/resources/application.yml`:

```yaml
app:
  security:
    jwt:
      issuer: ${JWT_ISSUER:https://edu.example.com}
      audience: ${JWT_AUDIENCE:edu-api}
      # Khóa bí mật Base64 mã hóa cho HS256 (tối thiểu 256 bits = 32 bytes)
      secret-base64: ${JWT_SECRET_BASE64:kX8vP9mN2qR5tU8x/A1bC4dE7gH0jK3mP6sV9yB2eE5=}
      access-token-ttl: ${JWT_ACCESS_TOKEN_TTL:PT15M}       # 15 phút (ISO-8601 Duration)
      refresh-token-ttl: ${JWT_REFRESH_TOKEN_TTL:P7D}       # 7 ngày (ISO-8601 Duration)
    cors:
      allowed-origins: ${CORS_ALLOWED_ORIGINS:http://localhost:3000,http://localhost:5173}
      allowed-methods: GET,POST,PUT,PATCH,DELETE,OPTIONS
      allowed-headers: Authorization,Content-Type,X-Organization-Id
      allow-credentials: true
```

Khai báo lớp cấu hình có kiểm tra tính hợp lệ (`@Validated`) tại `backend/boot/src/main/java/com/edu/boot/security/JwtProperties.java`:

```java
package com.edu.boot.security;

import java.time.Duration;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.security.jwt")
@Validated
public record JwtProperties(
    @NotBlank String issuer,
    @NotBlank String audience,
    @NotBlank @Size(min = 44, message = "secret-base64 must represent at least 256 bits") String secretBase64,
    @NotNull Duration accessTokenTtl,
    @NotNull Duration refreshTokenTtl
) {
}
```

---

### 5.3. Tiêu chí Hoàn thành Checkpoint 2.6.2

| ID | Tiêu chí | Trạng thái |
| :--- | :--- | :---: |
| **AC-2.6.2.1** | Kiểm tra dependency tree không xảy ra xung đột thư viện bảo mật | **DONE** |
| **AC-2.6.2.2** | Khai báo `spring-boot-starter-security` và `oauth2-resource-server` trong `boot/pom.xml` | **DONE** |
| **AC-2.6.2.3** | Cấu hình `JwtProperties` với validation chặt chẽ độ dài secret và TTL | **DONE** |
| **AC-2.6.2.4** | Ứng dụng khởi động bình thường, Flyway migration và context loading không bị lỗi | **DONE** |

---

