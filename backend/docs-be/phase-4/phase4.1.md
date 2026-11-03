# Phase 4.1 — Users, Tài khoản & Mã hóa Mật khẩu BCrypt

Tài liệu thiết kế kiến trúc, đặc tả kỹ thuật và tiêu chí nghiệm thu cho phân hệ quản lý người dùng và tài khoản định danh trong module `identity`.

---

## 1. Mục tiêu & Phạm vi

- **Quản lý danh tính người dùng**: Cung cấp cơ chế đăng ký, tra cứu thông tin tài khoản và cập nhật trạng thái (`ACTIVE`, `LOCKED`, `DISABLED`).
- **Bảo mật thông tin đăng nhập**: Mã hóa mật khẩu bằng thuật toán BCrypt với work factor tiêu chuẩn (`strength = 10`), tuyệt đối không lưu trữ plaintext password hay để lộ password hash ra ngoài API/DTO/logs.
- **Tính toàn vẹn dữ liệu**: Đảm bảo tính duy nhất của `username` và `email` trên toàn hệ thống ở cả mức JPA validation và cơ sở dữ liệu (`UNIQUE` index).
- **Phân tách Schema độc lập**: Quản lý bảng dữ liệu người dùng tại schema riêng biệt `identity.users` thông qua Flyway migration `V2__create_identity_users.sql`.

---

## 2. Tiêu chí Nghiệm thu (Acceptance Criteria)

| Mã AC | Tiêu chí | Chi tiết kỹ thuật |
| :---: | :--- | :--- |
| **AC-4.1.1** | **User Persistence & Migration** | Flyway migration `V2__create_identity_users.sql` tạo bảng `identity.users` với khóa chính UUID, các trường `username`, `email`, `password_hash`, `status`, `created_at`, `updated_at`. |
| **AC-4.1.2** | **Password Hashing với BCrypt** | `PasswordEncoderConfig` cấu hình `BCryptPasswordEncoder(10)`. Mật khẩu thô được băm trước khi lưu trữ vào database; không thể giải mã ngược lại. |
| **AC-4.1.3** | **Validation & Duplicate Prevention** | Ném ngoại lệ `DuplicateUsernameException` và `DuplicateEmailException` khi đăng ký trùng; trả mã lỗi HTTP 409 Conflict chuẩn hóa qua `ApiErrorResponse`. |
| **AC-4.1.4** | **Bảo vệ rò rỉ dữ liệu (DTO Protection)** | Record `AccountView` chỉ trả về thông tin công khai (`id`, `username`, `email`, `status`, `createdAt`); ArchUnit rule `AR-09` bảo đảm không chứa trường `passwordHash` hay `rawPassword`. |
| **AC-4.1.5** | **REST API Đăng ký & Tra cứu** | Endpoints `POST /api/v1/identity/accounts/register` và `GET /api/v1/identity/accounts/{id}` hoạt động chính xác với đầy đủ bean validation. |

---

## 3. Thiết kế Cơ sở dữ liệu & Entity

### 3.1. Flyway Migration (`V2__create_identity_users.sql`)

```sql
CREATE TABLE IF NOT EXISTS identity.users (
    id UUID PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_identity_users_username UNIQUE (username),
    CONSTRAINT uk_identity_users_email UNIQUE (email),
    CONSTRAINT ck_identity_users_status CHECK (status IN ('ACTIVE', 'LOCKED', 'DISABLED'))
);

CREATE INDEX IF NOT EXISTS idx_identity_users_status ON identity.users (status);
```

### 3.2. JPA Entity (`UserEntity.java`)

```java
package com.edu.identity.infrastructure.persistence.entity;

import com.edu.identity.domain.model.UserStatus;
import com.edu.shared.kernel.persistence.AuditableEntity;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "users", schema = "identity")
public class UserEntity extends AuditableEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "username", nullable = false, length = 50, unique = true)
    private String username;

    @Column(name = "email", nullable = false, length = 255, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private UserStatus status;

    protected UserEntity() {
        super();
    }

    public UserEntity(UUID id, String username, String email, String passwordHash, UserStatus status) {
        super();
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.status = status;
    }

    // Getters và business methods
}
```

---

## 4. Cấu hình Bảo mật Mật khẩu (`PasswordEncoderConfig.java`)

```java
package com.edu.identity.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordEncoderConfig {

    private static final int BCRYPT_STRENGTH = 10;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(BCRYPT_STRENGTH);
    }
}
```

---

## 5. Triển khai Nghiệp vụ (`IdentityService.java`)

```java
@Service
@Transactional
public class IdentityService {

    private final UserJpaRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public IdentityService(UserJpaRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AccountView registerAccount(RegisterAccountCommand command) {
        if (userRepository.existsByUsername(command.username())) {
            throw new DuplicateUsernameException(command.username());
        }
        if (userRepository.existsByEmail(command.email())) {
            throw new DuplicateEmailException(command.email());
        }

        String encodedPassword = passwordEncoder.encode(command.rawPassword());
        UUID userId = UUID.randomUUID();

        UserEntity entity = new UserEntity(
                userId,
                command.username(),
                command.email(),
                encodedPassword,
                UserStatus.ACTIVE
        );

        UserEntity saved = userRepository.save(entity);
        return mapToView(saved);
    }

    @Transactional(readOnly = true)
    public AccountView getAccountById(UserId userId) {
        return userRepository.findById(userId.value())
                .map(this::mapToView)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND, "User not found with id: " + userId));
    }
}
```

---

## 6. Hợp đồng REST API (Contract DTOs)

### Đăng ký tài khoản (`POST /api/v1/identity/accounts/register`)
- **Request Body**:
```json
{
  "username": "teacher.nguyen",
  "email": "teacher.nguyen@edu.vn",
  "password": "SecurePassword123!"
}
```
- **Response 201 Created**:
```json
{
  "id": "e9b2c3a4-1234-4567-89ab-cdef01234567",
  "username": "teacher.nguyen",
  "email": "teacher.nguyen@edu.vn",
  "status": "ACTIVE",
  "createdAt": "2026-03-30T10:00:00Z"
}
```

---

## 7. Liên kết Tài liệu Liên quan

- [Phase 4 README](README.md) — Tổng quan phân hệ Identity & Access
- [Phase 4.2: Organizations & Memberships](phase4.2.md) — Quản lý tổ chức và thành viên
- [Phase 4.3: Public Port Decoupling](phase4.3.md) — Phân tách phụ thuộc giữa Identity và Organization
- [Phase 4.4: JWT Token Management](phase4.4.md) — Cấp phát và xác thực JWT

