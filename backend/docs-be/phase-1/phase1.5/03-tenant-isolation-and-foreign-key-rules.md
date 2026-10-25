# Phase 1.5 — Thành phần 03: Tenant Isolation & Foreign Key Rules

Tài liệu này đặc tả cơ chế cô lập dữ liệu tổ chức (Multi-Tenancy) ở tầng cơ sở dữ liệu và các quy định bắt buộc về khóa ngoại (Foreign Keys).

---

## 1. Thiết Kế Cô Lập Dữ Liệu Tổ Chức (Tenant Isolation)

### 1.1. Cột `organization_id` Bắt Buộc
Mọi bảng dữ liệu phát sinh trong phạm vi vận hành của một trường học/tổ chức bắt buộc phải có cột:
```sql
organization_id UUID NOT NULL
```
- Trường này không cho phép giá trị `NULL`.
- Khi thực hiện bất kỳ câu truy vấn `SELECT`, `UPDATE`, `DELETE` nào từ tầng ứng dụng, bắt buộc phải đính kèm điều kiện lọc:
  ```sql
  WHERE organization_id = :tenantId AND ...
  ```

### 1.2. Ràng Buộc Khóa Duy Nhất Kết Hợp (Composite Unique Constraints)
Để hỗ trợ việc các trường học khác nhau có thể trùng mã số học sinh, mã phòng học hoặc mã lớp học phần mà không gây xung đột dữ liệu:
```sql
-- Ví dụ: Mã học sinh là duy nhất trong cùng một trường
CREATE UNIQUE INDEX uq_students_org_code 
ON people.students (organization_id, student_code);

-- Ví dụ: Tên phòng học là duy nhất trong cùng một trường
CREATE UNIQUE INDEX uq_rooms_org_name 
ON academic.rooms (organization_id, name);
```

---

## 2. Quy Tắc Khóa Ngoại: Intra-Schema vs Cross-Schema

```
┌────────────────────────────────────────────────────────┐
│ 1. CÙNG MỘT SCHEMA (Intra-Schema)                       │
│    -> Cho phép tạo Physical Foreign Key                │
│    -> Ví dụ: academic.class_sections -> course_offering │
│    -> Hỗ trợ toàn vẹn tham chiếu nội bộ module         │
├────────────────────────────────────────────────────────┤
│ 2. KHÁC SCHEMA (Cross-Schema)                          │
│    -> CẤM TUYỆT ĐỐI Physical Foreign Key ❌            │
│    -> Chỉ lưu Logical ID (UUID)                        │
│    -> Toàn vẹn được kiểm tra ở tầng Application / Port │
└────────────────────────────────────────────────────────┘
```

### Minh họa câu lệnh SQL chuẩn:
```sql
-- Trong schema academic (ĐÚNG ✅)
CREATE TABLE academic.class_sections (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    offering_id UUID NOT NULL,
    teacher_id UUID NOT NULL, -- Chỉ lưu UUID của giáo viên thuộc schema people
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Khóa ngoại nội bộ schema academic (HỢP LỆ):
    CONSTRAINT fk_class_sections_offering 
        FOREIGN KEY (offering_id) REFERENCES academic.course_offerings(id)
    
    -- TUYỆT ĐỐI KHÔNG TẠO:
    -- FOREIGN KEY (teacher_id) REFERENCES people.teachers(id) ❌
);
```
