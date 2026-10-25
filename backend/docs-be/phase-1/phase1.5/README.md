# Phase 1.5 — Persistence & Database Standards

Tài liệu này đặc tả các tiêu chuẩn thiết kế cơ sở dữ liệu PostgreSQL, quy chuẩn đặt tên bảng/cột, chiến lược cô lập dữ liệu tổ chức (Multi-Tenancy) và quy trình quản trị di trú schema bằng Flyway trong E-Edu.

---

## 1. Mục Tiêu Thiết Kế Cơ Sở Dữ Liệu

1. **Nhất quán 100%**: Áp dụng quy chuẩn đặt tên `snake_case`, kiểu dữ liệu chuẩn và trường auditing thống nhất trên toàn bộ các bảng.
2. **Cô lập ranh giới dữ liệu**: Mỗi module sở hữu schema riêng; không có khóa ngoại vật lý liên schema; dữ liệu tổ chức được phân tách bằng `organization_id`.
3. **Quản lý phiên bản chặt chẽ**: Toàn bộ thay đổi DDL/DML bắt buộc được phiên bản hóa qua Flyway migrations (`V{n}__{description}.sql`), cấm tuyệt đối dùng Hibernate `ddl-auto: update` trong môi trường sản xuất.

---

## 2. Danh Mục Tài Liệu Thành Phần Chi Tiết

| STT | Tài liệu | Mô tả chi tiết |
| :---: | :--- | :--- |
| **01** | [Database Naming & Typing Conventions](01-database-naming-and-typing-conventions.md) | Chuẩn đặt tên bảng/cột (`snake_case`), quy chuẩn UUID Primary Key, `TIMESTAMPTZ` (UTC), `NUMERIC` cho điểm số. |
| **02** | [Schema Separation & Table Catalog](02-schema-separation-and-table-catalog.md) | Danh mục chi tiết 12 Schemas, danh sách các bảng cốt lõi thuộc sở hữu của từng module. |
| **03** | [Tenant Isolation & Foreign Key Rules](03-tenant-isolation-and-foreign-key-rules.md) | Quy tắc `organization_id NOT NULL`, composite unique constraints và nguyên tắc cấm Cross-Schema Foreign Keys. |
| **04** | [Flyway Migration Strategy](04-flyway-migration-strategy.md) | Chiến lược di trú cơ sở dữ liệu: đánh số phiên bản, tính idempotent, quy trình kiểm soát lỗi và điều phối migration tập trung. |

---

## 3. Tiêu Chí Nghiệm Thu Hoàn Thành Phase 1.5

- [x] Thống nhất toàn bộ quy ước kiểu dữ liệu (UUID, `TIMESTAMPTZ`, `NUMERIC`, `JSONB`).
- [x] Phân chia ranh giới 12 schemas vật lý trên PostgreSQL.
- [x] Xác lập quy tắc cô lập tổ chức và composite index/unique constraint.
- [x] Chuẩn hóa quy trình viết và áp dụng Flyway migration an toàn.

---

> Chuyển tiếp sang: [Phase 1.6 — API Contracts & Cross-Module Communication](../phase1.6/README.md)
