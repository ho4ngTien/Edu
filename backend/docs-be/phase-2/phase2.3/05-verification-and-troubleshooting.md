# Phase 2.3 — Thành phần 05: Verification & Troubleshooting Guide

Tài liệu này hướng dẫn các bước kiểm chứng kết nối cơ sở dữ liệu, tra cứu lịch sử migration và xử lý các lỗi thường gặp trong Phase 2.3.

---

## 1. Các Bước Kiểm Chứng Kỹ Thuật

### 1.1. Kiểm Tra 12 Schemas Đã Được Tạo Trong PostgreSQL
Sử dụng công cụ `psql` kết nối vào container:
```bash
docker exec -it edu-postgres psql -U edu_user -d edu -c "\dn"
```

**Kết quả mong đợi:**
Danh sách gồm 12 schemas (`academic`, `assessment`, `attendance`, `certification`, `configuration`, `curriculum`, `enrollment`, `identity`, `organization`, `people`, `rule_engine`, `teaching`) và schema `public`.

### 1.2. Kiểm Tra Bảng Lịch Sử Flyway Migration
```bash
docker exec -it edu-postgres psql -U edu_user -d edu -c "SELECT installed_rank, version, description, type, success FROM public.flyway_schema_history;"
```

**Kết quả mong đợi:**
Bản ghi phiên bản `1` (`create initial schemas`) với cột `success = true`.

---

## 2. Xử Lý Các Sự Cố Thường Gặp (Troubleshooting)

| Sự cố | Nguyên nhân | Hướng khắc phục |
| :--- | :--- | :--- |
| `Connection to localhost:5432 refused` | Container PostgreSQL chưa chạy hoặc đang khởi động. | Chạy `docker compose up -d` và kiểm tra bằng `docker ps`. |
| `FATAL: password authentication failed for user` | Mật khẩu kết nối trong `application.yml` không khớp với môi trường Docker. | Kiểm tra biến `POSTGRES_PASSWORD` trong `docker-compose.yml` so với `DB_PASSWORD`. |
| `FlywayException: Validate failed: Migration checksum mismatch` | File migration đã chạy bị chỉnh sửa nội dung sau khi áp dụng. | Không sửa file cũ; tạo file migration mới hoặc chạy `flyway repair` nếu đang trong giai đoạn dev local. |
| `Schema-validation: missing table [identity.users]` | Hibernate `ddl-auto: validate` kiểm tra nhưng migration tạo bảng chưa được áp dụng. | Đảm bảo script migration tương ứng đã được chạy thành công qua Flyway trước khi Hibernate validate. |
