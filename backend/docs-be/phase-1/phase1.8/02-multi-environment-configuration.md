# Phase 1.8 — Thành phần 02: Multi-Environment Configuration

Tài liệu này đặc tả cơ chế quản lý cấu hình theo môi trường (**Spring Profiles**) và quy tắc bảo vệ bí mật (Secrets Management) trong E-Edu.

---

## 1. Phân Tách Cấu Hình Theo Profile

```
boot/src/main/resources/
├── application.yml              # Cấu hình chung mặc định cho toàn hệ thống
├── application-local.yml        # Cấu hình phục vụ phát triển trên máy local lập trình viên
├── application-test.yml         # Cấu hình kiểm thử tự động với H2 / Testcontainers
└── application-prod.yml         # Cấu hình môi trường sản xuất (bắt buộc nạp qua biến môi trường)
```

### 1.1. `application.yml` (Cấu hình chung)
Khai báo các tham số không nhạy cảm và giá trị fallback:
```yaml
spring:
  application:
    name: edu-backend
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:local}
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/edu}
    username: ${DB_USERNAME:edu_user}
    password: ${DB_PASSWORD:edu_secret}
  jpa:
    open-in-view: false
    hibernate:
      ddl-auto: validate
```

### 1.2. `application-test.yml` (Kiểm thử tự động)
Tối ưu hóa tốc độ chạy test và hỗ trợ Testcontainers / DynamicPropertySource:
```yaml
spring:
  flyway:
    enabled: true
  jpa:
    show-sql: false
```

---

## 2. Quy Tắc Bảo Vệ Bí Mật Môi Trường Sản Xuất (Prod Secrets)

1. **Nghiêm cấm hard-code thông tin nhạy cảm vào Git**:
   - Mật khẩu database (`DB_PASSWORD`).
   - Khóa ký JWT (`JWT_SECRET_BASE64`).
   - Khóa API của bên thứ ba (SMS gateway, cổng thanh toán).
2. **Cơ chế nạp giá trị trong môi trường Production**:
   - Sử dụng biến môi trường (Environment Variables) hoặc Docker Secrets / Kubernetes Secrets.
   - Khi khởi động trên môi trường `prod`, nếu các biến môi trường bắt buộc bị thiếu, ứng dụng phải lập tức dừng khởi động (`fail-fast`) và thông báo lỗi rõ ràng.
