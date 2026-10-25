# Phase 1.2 — Thành phần 04: Anti-Corruption & Data Isolation

Tài liệu này đặc tả các nguyên tắc bảo vệ ranh giới dữ liệu và tầng chống suy biến (Anti-Corruption Layer - ACL) nhằm ngăn chặn hiện tượng rò rỉ mô hình dữ liệu giữa các module trong Modular Monolith.

---

## 1. Nguyên Tắc Cấm Khóa Ngoại Vật Lý Liên Schema (No Cross-Schema Foreign Keys)

Trong thiết kế cơ sở dữ liệu của E-Edu:
- Mỗi module sở hữu một PostgreSQL schema riêng biệt (`identity`, `organization`, `academic`, `assessment`...).
- **Quy tắc bất biến:** Tuyệt đối không tạo Foreign Key vật lý (`REFERENCES other_schema.other_table`) xuyên qua các schema khác nhau!

```
       SAI (Vi phạm ranh giới module)
┌───────────────────────┐          ┌───────────────────────┐
│ Schema: academic      │          │ Schema: people        │
│ class_sections        │          │ teachers              │
│ ───────────────────── │          │ ───────────────────── │
│ id                    │          │ id                    │
│ teacher_id ───────────┼──────────┼▶ id (PHYSICAL FK ❌)  │
└───────────────────────┘          └───────────────────────┘

       ĐÚNG (Modular Monolith Standard)
┌───────────────────────┐          ┌───────────────────────┐
│ Schema: academic      │          │ Schema: people        │
│ class_sections        │          │ teachers              │
│ ───────────────────── │          │ ───────────────────── │
│ id                    │          │ id                    │
│ teacher_id (UUID)     │          │ full_name, rank, ...  │
│ [Logical ID Ref ✅]   │          │                       │
└───────────────────────┘          └───────────────────────┘
```

### Lý Do Thiết Kế:
1. **Khả năng Độc lập Di trú (Migration Independence)**: Khi chạy Flyway migration cho schema `academic`, migration không bị phụ thuộc vào thứ tự tạo bảng vật lý của schema `people`.
2. **Sẵn sàng Chia Tách (Microservices Ready)**: Nếu sau này một module (ví dụ: `assessment` hoặc `enrollment`) cần tách ra thành dịch vụ độc lập với database riêng, cấu trúc dữ liệu không cần phải gỡ bỏ hàng loạt khóa ngoại vật lý phức tạp.
3. **Hiệu năng và Khóa Bảng (Locking & Contention)**: Tránh việc PostgreSQL phải kiểm tra khóa ngoại xuyên schema gây lock chéo bảng trong các transaction đồng thời.

---

## 2. Kiểm Soát Tính Toàn Vẹn Bằng Logic Ứng Dụng (Logical Integrity)

Khi không dùng khóa ngoại vật lý xuyên schema, tính toàn vẹn được bảo đảm qua 3 lớp phòng vệ:

1. **Validation tại Tầng Ứng Dụng (Application Layer Validation)**:
   - Trước khi tạo bản ghi mới (ví dụ: gán `teacher_id` cho một `ClassSection`), service gọi Public Port của module `people` để xác nhận ID đó tồn tại và đang ở trạng thái hoạt động (`ACTIVE`).
2. **Kiểm Tra Ngữ Cảnh Tổ Chức (Tenant Scoping)**:
   - Xác nhận rằng đối tượng được tham chiếu thuộc cùng một `organization_id` với bản ghi hiện tại.
3. **Xử Lý Xóa/Vô Hiệu Hóa Dữ Liệu (Soft Deactivation)**:
   - Tuyệt đối không thực hiện `CASCADE DELETE` qua các module.
   - Thay vào đó, khi một thực thể (ví dụ: `Course`) bị dừng hoạt động, trạng thái chuyển sang `DEACTIVATED` hoặc `ARCHIVED`. Module `curriculum` phát ra Domain Event `CourseArchivedEvent` để các module khác cập nhật cờ cảnh báo tương ứng.
