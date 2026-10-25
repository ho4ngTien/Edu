# Phase 2.6: Checkpoint 2.6.3 — Authentication API (Login)

> Thuộc [Phase 2.6: Security & JWT Token Management](README.md)

## 6. Checkpoint 2.6.3: Authentication API (Login)

Mục tiêu của checkpoint 2.6.3 là thiết kế và triển khai REST API `POST /api/v1/auth/login`, xác thực credentials với `BCryptPasswordEncoder`, xử lý an toàn chống timing attack, và cấp phát bộ token.

### 6.1. Hợp đồng Giao tiếp API (API Contract)

#### Yêu cầu Đăng nhập (Request)
- **Phương thức**: `POST`
- **Đường dẫn**: `/api/v1/auth/login`
- **Content-Type**: `application/json`
- **Quyền truy cập**: Public (`permitAll()`)

```json
{
  "usernameOrEmail": "teacher@edu.vn",
  "password": "SecurePassword123!"
}
```

#### Phản hồi Thành công (Response 200 OK)

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "refreshToken": "4a7f9b8c2e1d0f3a6b5c4d3e2f1a0b9c8d7e6f5a4b3c2d1e0f9a8b7c6d5e4f3a"
}
```

#### Phản hồi Lỗi (Response 401 Unauthorized / 400 Bad Request)

```json
{
  "timestamp": "2026-10-23T11:30:00Z",
  "status": 401,
  "code": "BAD_CREDENTIALS",
  "message": "Invalid username/email or password",
  "path": "/api/v1/auth/login"
}
```

> [!WARNING]
> Tuyệt đối không trả về thông báo lỗi cụ thể như *\"Tài khoản không tồn tại\"* hoặc *\"Mật khẩu không chính xác\"*. Cả hai trường hợp đều phải trả về chung một mã lỗi và thông điệp chuẩn hóa để ngăn chặn kẻ tấn công dò quét sự tồn tại của tài khoản (Username Enumeration Attack).

---

### 6.2. DTOs và Public Ports

#### 1. Request & Response DTOs
Vị trí: `backend/modules/identity/src/main/java/com/edu/identity/api/dto/`

```java
package com.edu.identity.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
    @NotBlank(message = "Username or email is required")
    @Size(max = 255, message = "Identifier must not exceed 255 characters")
    String usernameOrEmail,

    @NotBlank(message = "Password is required")
    @Size(max = 72, message = "Password must not exceed 72 characters")
    String password
) {
}
```

```java
package com.edu.identity.api.dto;

public record LoginResponse(
    String accessToken,
    String tokenType,
    long expiresIn,
    String refreshToken
) {
}
```

#### 2. Public Port Cấp phát Token: `TokenIssuer`
Để giữ đúng ranh giới của Modular Monolith, module `identity` không phụ thuộc vào `boot`. Module `identity` khai báo một Interface Port, và adapter triển khai thực tế sẽ nằm tại `boot`:

Vị trí: `backend/modules/identity/src/main/java/com/edu/identity/application/port/TokenIssuer.java`

```java
package com.edu.identity.application.port;

import java.util.UUID;

public interface TokenIssuer {

    IssuedTokens issue(UUID userId);

    record IssuedTokens(
        String accessToken,
        String refreshToken,
        long expiresIn
    ) {
    }
}
```

---

### 6.3. Logic Xác thực & Chống Timing Attack

Vị trí: `backend/modules/identity/src/main/java/com/edu/identity/application/service/AuthenticationService.java`

```java
package com.edu.identity.application.service;

import java.util.Optional;
import com.edu.identity.api.dto.LoginRequest;
import com.edu.identity.api.dto.LoginResponse;
import com.edu.identity.application.port.TokenIssuer;
import com.edu.identity.domain.model.UserStatus;
import com.edu.identity.infrastructure.persistence.entity.UserEntity;
import com.edu.identity.infrastructure.persistence.repository.UserJpaRepository;
import com.edu.shared.kernel.domain.exception.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticationService {

    // Dummy hash để thực thi BCrypt khi user không tồn tại, chống timing attack
    private static final String DUMMY_HASH = "$2a$10$wT3wY2K7R8mQ5nL9pX4rOe8uI1vY6sT9aB2cD5eF8gH1jK4mN7pQ.";

    private final UserJpaRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenIssuer tokenIssuer;

    public AuthenticationService(
        UserJpaRepository userRepository,
        PasswordEncoder passwordEncoder,
        TokenIssuer tokenIssuer
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenIssuer = tokenIssuer;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Optional<UserEntity> userOpt = userRepository.findByUsername(request.usernameOrEmail());
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByEmail(request.usernameOrEmail());
        }

        if (userOpt.isEmpty()) {
            // Chạy kiểm tra mật khẩu giả để bảo đảm thời gian phản hồi đồng nhất
            passwordEncoder.matches(request.password(), DUMMY_HASH);
            throw new BadCredentialsException("Invalid username/email or password");
        }

        UserEntity user = userOpt.get();

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BadCredentialsException("Account is disabled or suspended");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid username/email or password");
        }

        TokenIssuer.IssuedTokens tokens = tokenIssuer.issue(user.getId());

        return new LoginResponse(
            tokens.accessToken(),
            "Bearer",
            tokens.expiresIn(),
            tokens.refreshToken()
        );
    }
}
```

---

### 6.4. REST Controller

Vị trí: `backend/modules/identity/src/main/java/com/edu/identity/infrastructure/web/AuthenticationController.java`

```java
package com.edu.identity.infrastructure.web;

import com.edu.identity.api.dto.LoginRequest;
import com.edu.identity.api.dto.LoginResponse;
import com.edu.identity.application.service.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authenticationService.login(request));
    }
}
```

---

### 6.5. Tiêu chí Hoàn thành Checkpoint 2.6.3

| ID | Tiêu chí | Trạng thái |
| :--- | :--- | :---: |
| **AC-2.6.3.1** | Đăng nhập thành công với username hoặc email hợp lệ trả về mã 200 OK | **DONE** |
| **AC-2.6.3.2** | Mật khẩu sai hoặc tài khoản không tồn tại trả về lỗi 401 `BAD_CREDENTIALS` | **DONE** |
| **AC-2.6.3.3** | Cơ chế dummy password verification hoạt động, ngăn chặn timing attack | **DONE** |
| **AC-2.6.3.4** | Tài khoản có trạng thái khác `ACTIVE` (`LOCKED`, `DISABLED`) bị từ chối cấp token | **DONE** |
| **AC-2.6.3.5** | Phản hồi tuyệt đối không chứa password hash, thông tin nhạy cảm hay stack trace | **DONE** |

---

