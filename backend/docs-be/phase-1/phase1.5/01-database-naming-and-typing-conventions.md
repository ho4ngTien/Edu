# Phase 1.5 — Thành phần 01: Database Naming & Typing Conventions

Tài liệu này đặc tả các quy ước bắt buộc về định danh, kiểu dữ liệu và trường theo dõi dữ liệu (Auditing) áp dụng cho mọi bảng trong cơ sở dữ liệu PostgreSQL của E-Edu.

---

## 1. Bảng Quy Ước Kiểu Dữ Liệu & Quy Chuẩn Đặt Tên

| Hạng mục | Quy chuẩn E-Edu | Ví dụ cụ thể | Giải thích kỹ thuật |
| :--- | :--- | :--- | :--- |
| **Primary Key** | `UUID` | `id UUID PRIMARY KEY` | Tạo ngẫu nhiên bằng v4; không dùng số nguyên tự tăng (`BIGSERIAL`) để tránh lộ số lượng người dùng hoặc bị tấn công duyệt số thứ tự. |
| **Tên Schema** | `snake_case`, danh từ số ít | `identity`, `academic` | Trùng với tên Maven module Bounded Context. |
| **Tên Bảng** | `snake_case`, danh từ số nhiều | `users`, `class_sections` | Luôn dùng số nhiều (`memberships`, `courses`). |
| **Tên Cột** | `snake_case` | `organization_id`, `created_at` | Rõ nghĩa, không viết tắt mơ hồ. |
| **Thời gian (Timestamp)**| `TIMESTAMPTZ` | `created_at TIMESTAMPTZ NOT NULL` | Luôn lưu múi giờ UTC; Java tương ứng dùng `java.time.Instant`. |
| **Ngày tháng (Date)** | `DATE` | `date_of_birth DATE` | Chỉ lưu ngày, tháng, năm; Java tương ứng dùng `java.time.LocalDate`. |
| **Điểm số & Tiền tệ** | `NUMERIC(p, s)` | `score NUMERIC(5, 2)` | Đảm bảo độ chính xác tuyệt đối; Java tương ứng dùng `java.math.BigDecimal`. |
| **Trạng thái (Enum)** | `VARCHAR(32)` hoặc `VARCHAR(50)` | `status VARCHAR(32) NOT NULL` | Lưu mã chuỗi chữ hoa (`ACTIVE`, `SUSPENDED`); tránh dùng PostgreSQL native enum để dễ di trú và nâng cấp. |
| **Dữ liệu động** | `JSONB` | `metadata JSONB` | Dùng cho cấu hình tùy biến, lưu log hoặc tham số linh hoạt. |

---

## 2. Tiêu Chuẩn Các Trường Auditing & Optimistic Locking

Mọi bảng nghiệp vụ trong hệ thống bắt buộc phải có tối thiểu các cột sau:

```sql
created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
```

Với các thực thể có nguy cơ chỉnh sửa đồng thời cao (như đăng ký học phần, đổi trạng thái thanh toán, cập nhật điểm thi):
```sql
version BIGINT NOT NULL DEFAULT 0
```
- Trường `version` được ánh xạ qua annotation `@Version` của JPA để Hibernate tự động kiểm tra Optimistic Locking, ngăn ngừa hiện tượng mất mát dữ liệu do ghi đè (Lost Update Problem).
