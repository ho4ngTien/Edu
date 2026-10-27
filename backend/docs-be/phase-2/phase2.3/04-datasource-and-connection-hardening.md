# Phase 2.3 — Thành phần 04: DataSource & Connection Hardening

Tài liệu này đặc tả cấu hình Spring Boot DataSource với HikariCP connection pool và các biện pháp bảo vệ kết nối CSDL.

---

## 1. Cấu Hình HikariCP Connection Pool

Tối ưu hóa số lượng kết nối và thời gian chờ để ngăn chặn cạn kiệt connection (Connection Starvation):

```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/edu}
    username: ${DB_USERNAME:edu_user}
    password: ${DB_PASSWORD:edu_secret}
    driver-class-name: org.postgresql.Driver
    hikari:
      pool-name: EEduHikariPool
      maximum-pool-size: 15
      minimum-idle: 5
      idle-timeout: 300000
      max-lifetime: 1800000
      connection-timeout: 20000
      validation-timeout: 3000
```

---

## 2. Các Biện Pháp Bảo Vệ An Toàn

1. **Không Hard-code Mật khẩu trong Môi trường Staging/Prod**:
   - Sử dụng cú pháp `${DB_PASSWORD:edu_secret}` để hỗ trợ giá trị mặc định cho local dev và bắt buộc nạp biến môi trường trên server production.
2. **Tắt Open-In-View (`spring.jpa.open-in-view: false`)**:
   - Ngăn chặn việc giữ kết nối Database connection xuyên suốt chu kỳ render của tầng web, giải phóng connection ngay sau khi Service hoàn tất `@Transactional`.
3. **Cấu hình JDBC Batching**:
   - Bật `hibernate.jdbc.batch_size: 25` để gộp nhiều câu lệnh INSERT/UPDATE thành một batch, giảm số lần gửi round-trip qua mạng tới database server.
