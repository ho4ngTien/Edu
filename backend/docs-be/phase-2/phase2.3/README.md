# Phase 2.3 — PostgreSQL & Flyway Multi-Schema

Tài liệu này đặc tả quy trình kết nối E-Edu với **PostgreSQL** chạy qua Docker Compose, cấu hình quản trị di trú schema theo phiên bản bằng **Flyway**, thiết lập datasource an toàn và hiện thực hóa kiến trúc **Multi-Schema Modular Monolith**.

---

## 1. Mục Tiêu Kỹ Thuật

1. **Hạ tầng CSDL Docker hóa**: Khởi chạy container PostgreSQL 15 Alpine (`edu-postgres`) qua `docker-compose.yml`, mở cổng 5432, lưu trữ persistent volume.
2. **Kiến trúc Dữ liệu Multi-Schema**: 1 Database vật lý `edu` chứa 12 schemas độc lập theo module; tuân thủ nghiêm ngặt quy tắc cấm khóa ngoại vật lý liên schema (No Cross-Schema Foreign Keys).
3. **Quản lý Migration Chặt Chẽ với Flyway**: Toàn bộ thay đổi cấu trúc database được quản lý qua các file SQL `V{n}__{description}.sql`, Hibernate chạy ở chế độ `ddl-auto: validate`.

---

## 2. Danh Mục Tài Liệu Thành Phần Chi Tiết

| STT | Tài liệu | Mô tả chi tiết |
| :---: | :--- | :--- |
| **01** | [Docker Compose & PostgreSQL Setup](01-docker-compose-postgresql-setup.md) | Thiết lập container `edu-postgres`, file `docker-compose.yml`, persistent volume và các biến môi trường cấu hình. |
| **02** | [Multi-Schema Architecture & Isolation](02-multi-schema-architecture-and-isolation.md) | Phân tách 12 schemas theo module, nguyên tắc No Cross-Schema Physical FKs, cơ chế cô lập tổ chức qua `organization_id`. |
| **03** | [Flyway Migration Management](03-flyway-migration-management.md) | Quy chuẩn đặt tên file migration, script khởi tạo 12 schemas `V1__create_initial_schemas.sql` và tính idempotent. |
| **04** | [DataSource & Connection Hardening](04-datasource-and-connection-hardening.md) | Cấu hình HikariCP connection pool, nạp DataSource từ biến môi trường, bảo vệ an toàn thông tin kết nối. |
| **05** | [Verification & Troubleshooting](05-verification-and-troubleshooting.md) | Hướng dẫn kiểm chứng kết nối, truy vấn bảng `flyway_schema_history` và các bước khắc phục sự cố kết nối CSDL. |

---

## 3. Tiêu Chí Nghiệm Thu Hoàn Thành Phase 2.3

- [x] Container PostgreSQL khởi chạy ổn định qua Docker Compose.
- [x] Flyway migration khởi tạo thành công 12 schemas độc lập trong CSDL.
- [x] Kết nối DataSource từ Spring Boot hoàn tất, Hibernate xác thực schema thành công (`ddl-auto: validate`).
- [x] Bảng `public.flyway_schema_history` ghi nhận chính xác phiên bản `V1`.

---

> Chuyển tiếp sang: [Phase 2.4 — Shared Kernel & Domain Foundation](../phase2.4/README.md)
