# School Management System (SMS)

Hệ thống Quản lý Trường học (School Management System) được thiết kế theo chuẩn **Modular Monolith Architecture**. Dự án được phân tách thành 3 cổng Frontend (Portals) và 1 Backend duy nhất nhưng bên trong chia thành 8 Modules độc lập chạy chung trên một hệ quản trị cơ sở dữ liệu PostgreSQL.

## Kiến Trúc Hệ Thống

### Frontend (Client App)
- Là một ứng dụng **Single Page Application (SPA)** duy nhất.
- Hệ thống định tuyến dựa trên vai trò (Role-Based Routing) sau khi đăng nhập thành công:
  - `/student/*` : Phân hệ dành cho học sinh (Xem điểm, lịch học, nộp bài tập).
  - `/teacher/*` : Phân hệ dành cho giáo viên (Nhập điểm, điểm danh, giao bài tập).
  - `/admin/*`   : Phân hệ quản trị hệ thống (Quản lý users, cấu hình trường học, báo cáo).

### Backend (Modular Monolith)
Ứng dụng Backend Spring Boot duy nhất, chia thành các gói (packages) độc lập (Bounded Contexts). Mỗi Module quản lý một Schema độc lập, giao tiếp với nhau qua Java Interfaces nội bộ (hoặc In-memory Events) và liên kết dữ liệu ở tầng DB bằng **Soft Links** (UUID) thay vì Foreign Key.

1. **Auth Module**: Đăng nhập, JWT, Phân quyền (RBAC).
2. **User Module**: Quản lý hồ sơ, nhân sự (Student, Teacher, Staff).
3. **Academic Module**: Quản lý khối lớp, môn học, năm học và thời khóa biểu.
4. **LMS Module**: Hệ thống học tập (giao bài, nộp bài, tài liệu).
5. **Grade Module**: Quản lý điểm số, bảng điểm tổng kết (GPA).
6. **Attendance Module**: Quản lý điểm danh và chuyên cần.
7. **Notification Module**: Gửi email, thông báo in-app, nhắc nhở deadline.
8. **Admin Module**: Báo cáo tổng hợp, cấu hình hệ thống.

## Tài Liệu Thiết Kế Dữ Liệu (ERD)

Các file cấu trúc Database (DBML) cho 8 Microservices được lưu trữ tại thư mục `docs/erd/`:

| Microservice | File DBML | dbdiagram.io |
|---|---|---|
| Auth | [db_auth.dbml](docs/erd/db_auth.dbml) | [Link dbdiagram.io] |
| User | [db_user.dbml](docs/erd/db_user.dbml) | [Link dbdiagram.io] |
| Academic | [db_academic.dbml](docs/erd/db_academic.dbml) | [Link dbdiagram.io] |
| LMS | [db_lms.dbml](docs/erd/db_lms.dbml) | [Link dbdiagram.io] |
| Grade | [db_grade.dbml](docs/erd/db_grade.dbml) | [Link dbdiagram.io] |
| Attendance | [db_attendance.dbml](docs/erd/db_attendance.dbml) | [Link dbdiagram.io] |
| Notification| [db_notification.dbml](docs/erd/db_notification.dbml) | [Link dbdiagram.io] |
| Admin | [db_admin.dbml](docs/erd/db_admin.dbml) | [Link dbdiagram.io] |

> **Hướng dẫn:** Sử dụng nội dung file `.dbml` chép vào [dbdiagram.io](https://dbdiagram.io) để xem sơ đồ ERD trực quan.

## Tài liệu Kỹ thuật khác
- [Kiến trúc chi tiết (Architecture)](docs/architecture_specifications.md)
- [Luồng nghiệp vụ (Business Flows)](docs/business_flows_and_use_cases.md)
