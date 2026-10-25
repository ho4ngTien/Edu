# Phase 1.5 — Thành phần 04: Flyway Migration Strategy

Tài liệu này đặc tả quy trình quản lý di trú cơ sở dữ liệu theo phiên bản bằng **Flyway**, đảm bảo tính toàn vẹn, khả năng khôi phục và quy chuẩn phối hợp đa module trong E-Edu.

---

## 1. Nguyên Tắc Cốt Lõi Khi Quản Lý Migrations

1. **Chỉ tiến về phía trước (Forward-only)**:
   - Tuyệt đối không sửa đổi nội dung của một file migration đã được áp dụng (đã commit và chạy trên bất kỳ môi trường nào).
   - Nếu có lỗi cú pháp hoặc muốn thay đổi cột: bắt buộc tạo một file migration phiên bản mới (`V{n+1}`) để sửa đổi.
2. **Không dùng Hibernate Auto-DDL**:
   - Trong môi trường `local`, `test`, `staging` và `prod`: Hibernate bắt buộc cấu hình `ddl-auto: validate`.
   - Mọi thay đổi schema đều do Flyway thực hiện trước khi Spring Context nạp EntityManager.
3. **Tính Idempotent (Khởi động an toàn lặp lại)**:
   - Script migration phải chạy an toàn; nếu bảng hoặc index đã tồn tại theo kịch bản có điều kiện, phải dùng cú pháp an toàn (`CREATE TABLE IF NOT EXISTS`, `CREATE INDEX IF NOT EXISTS`).

---

## 2. Quy Ước Đặt Tên & Đánh Số Phiên Bản

Định dạng file migration:
```
V{Phiên_bản}__{Mô_tả_ngắn_gọn}.sql
```
- Sử dụng 2 dấu gạch dưới `__` ngăn cách giữa số phiên bản và phần mô tả.
- Phần mô tả viết bằng chữ thường, dùng dấu gạch dưới thay cho khoảng trắng (`snake_case`).

### Lộ trình đánh số phiên bản trong E-Edu:
- **`V1__create_initial_schemas.sql`**: Khởi tạo 12 PostgreSQL schemas (`identity`, `organization`,...).
- **`V2__create_identity_users.sql`**: Tạo bảng `identity.users`.
- **`V3__create_organizations.sql`**: Tạo bảng `organization.organizations`.
- **`V4__create_organization_memberships.sql`**: Tạo bảng `organization.organization_memberships`.
- **`V5__create_identity_refresh_tokens.sql`**: Tạo bảng `identity.refresh_tokens` (Phase 2.6).

---

## 3. Quản Lý Vị Trí Lưu Trữ File Migration

Để vừa bảo đảm tính sở hữu của từng module vừa bảo đảm khả năng khởi chạy tập trung:
- Mỗi module có thể chứa file migration của chính mình tại: `src/main/resources/db/migration/`.
- Module `boot` (Composition Root) tập hợp các file migration tại: `boot/src/main/resources/db/migration/` để điều phối chạy tuần tự khi ứng dụng khởi động.
- Flyway tự động ghi nhận lịch sử vào bảng hệ thống: `public.flyway_schema_history`.
