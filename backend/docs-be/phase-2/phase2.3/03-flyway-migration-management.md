# Phase 2.3 — Thành phần 03: Flyway Migration Management

Tài liệu này đặc tả quy trình quản lý các tệp migration của Flyway và nội dung migration ban đầu `V1__create_initial_schemas.sql`.

---

## 1. Nội Dung File Migration `V1__create_initial_schemas.sql`

Tệp đặt tại: `boot/src/main/resources/db/migration/V1__create_initial_schemas.sql`

```sql
-- Khởi tạo 12 PostgreSQL Schemas độc lập cho các Bounded Contexts của E-Edu
CREATE SCHEMA IF NOT EXISTS identity;
CREATE SCHEMA IF NOT EXISTS organization;
CREATE SCHEMA IF NOT EXISTS configuration;
CREATE SCHEMA IF NOT EXISTS people;
CREATE SCHEMA IF NOT EXISTS curriculum;
CREATE SCHEMA IF NOT EXISTS academic;
CREATE SCHEMA IF NOT EXISTS enrollment;
CREATE SCHEMA IF NOT EXISTS teaching;
CREATE SCHEMA IF NOT EXISTS attendance;
CREATE SCHEMA IF NOT EXISTS assessment;
CREATE SCHEMA IF NOT EXISTS certification;
CREATE SCHEMA IF NOT EXISTS rule_engine;
```

---

## 2. Cơ Chế Điều Phối Migration Tập Trung

1. **Vị trí tài nguyên**:
   - Module `boot` đóng vai trò là Composition Root điều phối chạy toàn bộ migrations khi ứng dụng khởi động từ thư mục `boot/src/main/resources/db/migration/`.
2. **Bảng lịch sử di trú**:
   - Flyway tự động tạo bảng `public.flyway_schema_history` để lưu vết checksum, ngày chạy và trạng thái của từng migration script.
3. **Tính Idempotent**:
   - Sử dụng câu lệnh `CREATE SCHEMA IF NOT EXISTS` để đảm bảo khi chạy lại không gây ra lỗi trùng lặp.
