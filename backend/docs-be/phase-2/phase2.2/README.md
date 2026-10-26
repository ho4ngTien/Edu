# Phase 2.2 — Spring Boot Bootstrap & Profiles

Tài liệu này đặc tả cơ chế khởi động ứng dụng (**Application Bootstrap**), tổ chức các tệp cấu hình đa môi trường (**Spring Profiles**) và thiết lập các smoke tests kiểm tra Spring Context cho E-Edu.

---

## 1. Mục Tiêu Kỹ Thuật

1. **Khởi động Spring Boot 3.4.3 ổn định**: Điểm vào duy nhất tại `com.edu.boot.EduApplication` quét và nạp chính xác các module theo kiến trúc Modular Monolith.
2. **Cấu hình đa môi trường an toàn**: Phân định rõ ràng giữa cấu hình mặc định, cấu hình môi trường phát triển local (`application-local.yml`), môi trường kiểm thử (`application-test.yml`) và môi trường sản xuất (`application-prod.yml`).
3. **Smoke testing đảm bảo chất lượng**: Kiểm chứng Spring Application Context khởi động thành công trong các bài test `EduApplicationTests.java`.

---

## 2. Danh Mục Tài Liệu Thành Phần Chi Tiết

| STT | Tài liệu | Mô tả chi tiết |
| :---: | :--- | :--- |
| **01** | [Spring Boot Entrypoint](01-spring-boot-application-entrypoint.md) | Đặc tả lớp `EduApplication.java`, cấu hình `@SpringBootApplication`, `@EntityScan`, `@EnableJpaRepositories`. |
| **02** | [Application Profiles & Config](02-application-profiles-and-configuration.md) | Tổ chức `application.yml`, `application-local.yml`, `application-test.yml`, nạp biến môi trường và kết nối DB. |
| **03** | [Properties Validation & Fail-Fast](03-configuration-properties-validation.md) | Kiểm tra tính hợp lệ của cấu hình bắt buộc khi ứng dụng khởi động, cơ chế Fail-Fast bảo vệ hệ thống. |
| **04** | [Smoke Tests & Verification](04-smoke-tests-and-verification.md) | Bộ kiểm thử `EduApplicationTests`, cách chạy test khởi động và xác minh tính toàn vẹn của Spring Context. |

---

## 3. Tiêu Chí Nghiệm Thu Hoàn Thành Phase 2.2

- [x] Lớp `EduApplication.java` khởi chạy thành công Spring Boot 3.4.3 với Java 21.
- [x] Thiết lập đầy đủ các profile cấu hình `local`, `test`, `prod`.
- [x] Không hard-code mật khẩu hoặc URL cơ sở dữ liệu production trong file Git.
- [x] Lệnh `mvn test -pl boot` vượt qua toàn bộ smoke tests.

---

> Chuyển tiếp sang: [Phase 2.3 — PostgreSQL & Flyway Multi-Schema](../phase2.3/README.md)
