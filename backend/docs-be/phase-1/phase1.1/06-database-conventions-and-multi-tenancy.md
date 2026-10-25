# Phase 1.1 — Thành phần 06: Database Conventions & Multi-Tenancy

Tài liệu này tổng hợp các quyết định kỹ thuật nền tảng về cơ sở dữ liệu PostgreSQL, quy ước định danh và mô hình cô lập dữ liệu tổ chức (Multi-Tenancy) cho E-Edu.

---

## 1. Quy Chuẩn Đặt Tên & Kiểu Dữ Liệu

| Hạng mục | Quy chuẩn E-Edu | Ví dụ cụ thể | Ghi chú kỹ thuật |
| :--- | :--- | :--- | :--- |
| **Hệ Quản trị CSDL** | PostgreSQL 15+ | Docker container `edu-postgres` | Hỗ trợ JSONB, UUID v4, Schema cô lập. |
| **Primary Key** | `UUID` | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` | Tránh lộ số lượng bản ghi và phòng chống Enumeration Attack. |
| **Tên Schema** | `snake_case`, danh từ số ít | `identity`, `organization`, `academic` | Tương ứng với từng module Bounded Context. |
| **Tên Bảng** | `snake_case`, danh từ số nhiều | `users`, `class_sections`, `assessments` | Nhất quán trong toàn bộ database. |
| **Tên Cột** | `snake_case` | `created_at`, `student_code`, `max_score` | Không viết hoa, không dấu tiếng Việt. |
| **Java Entity** | `PascalCase`, danh từ số ít | `UserEntity`, `ClassSectionEntity` | Đặt trong package `infrastructure/persistence/entity`. |
| **Thời gian (Timestamp)** | `TIMESTAMPTZ` | `2026-10-25 12:30:00+00` | Luôn lưu mốc thời gian theo chuẩn UTC; Java dùng `Instant`. |
| **Ngày tháng (Date)** | `DATE` | `2005-09-15` | Áp dụng cho ngày sinh, ngày nhập học; Java dùng `LocalDate`. |
| **Điểm số & Tiền tệ** | `NUMERIC(p, s)` | `NUMERIC(5, 2)` | Đảm bảo độ chính xác tuyệt đối; Java dùng `BigDecimal`. |
| **Auditing Fields** | `created_at`, `updated_at` | `TIMESTAMPTZ NOT NULL` | Tự động cập nhật qua Spring Data JPA Auditing. |

---

## 2. Chiến Lược Multi-Tenancy (ADR-001)

E-Edu áp dụng mô hình **Shared Database, Shared Tables** phân tách theo `organization_id`:

```
┌────────────────────────────────────────────────────────────────────────┐
│                        PostgreSQL Database: edu                        │
├────────────────────────────────────────────────────────────────────────┤
│  Schema: organization                                                  │
│    - organizations (id, name, code, status)                            │
│    - organization_memberships (id, organization_id, user_id, status)   │
├────────────────────────────────────────────────────────────────────────┤
│  Schema: academic                                                      │
│    - academic_periods (id, organization_id, name, start_date, ...)     │
│    - course_offerings (id, organization_id, course_id, period_id, ...) │
│    - class_sections (id, organization_id, offering_id, name, ...)      │
└────────────────────────────────────────────────────────────────────────┘
```

### Nguyên tắc Bắt buộc:
1. **Mọi bảng nghiệp vụ thuộc phạm vi tổ chức** bắt buộc có cột `organization_id UUID NOT NULL`.
2. **Không tin cậy client**: Giá trị `organization_id` tuyệt đối không được lấy tự do từ request body hoặc query parameter mà phải được trích xuất từ `SecurityContext` / `TenantContext` sau khi xác thực thành công.
3. **Ràng buộc duy nhất theo tổ chức (Composite Unique Constraint)**:
   - Ví dụ: Mã học sinh `student_code` là duy nhất trong một trường, nhưng hai trường khác nhau có thể có cùng mã:
     ```sql
     CONSTRAINT uq_students_org_code UNIQUE (organization_id, student_code)
     ```

---

## 3. Ánh Xạ Domain Model vào 11 Module Modular Monolith

| STT | Module | Schema PostgreSQL | Các Thực Thể Chính |
| :---: | :--- | :--- | :--- |
| 1 | `identity` | `identity` | `users`, `roles`, `permissions`, `user_roles`, `refresh_tokens` |
| 2 | `organization` | `organization` | `organizations`, `organizational_units`, `organization_memberships` |
| 3 | `configuration` | `configuration` | `system_settings`, `organization_policies`, `assessment_schemes` |
| 4 | `people` | `people` | `persons`, `students`, `teachers`, `guardians`, `student_guardians` |
| 5 | `curriculum` | `curriculum` | `programs`, `program_versions`, `courses`, `program_courses` |
| 6 | `academic` | `academic` | `academic_periods`, `class_groups`, `course_offerings`, `class_sections`, `rooms` |
| 7 | `enrollment` | `enrollment` | `program_enrollments`, `course_enrollments` |
| 8 | `teaching` | `teaching` | `teaching_assignments`, `teacher_schedules` |
| 9 | `attendance` | `attendance` | `attendance_records`, `attendance_summaries` |
| 10 | `assessment` | `assessment` | `assessments`, `assessment_results`, `grade_reviews` |
| 11 | `certification` | `certification` | `certificates`, `graduation_records` |
