# Backend Checklist: Module 03 - Academic Core

**Trạng thái:** ⏳ Pending
**Tiến độ:** 0%

## Domain Entities & Repositories
- [ ] Khởi tạo `Semester`, `Course`, `AcademicClass`, `Schedule`.
- [ ] Thiết lập Repository cho các Entity trên.
- [ ] Viết các DTO trả về danh sách lớp học kèm Tên Giáo Viên (chuyển đổi từ `teacherId`).

## API: Học kỳ & Môn học (Semester & Course)
- [ ] CRUD API `Semester` (Chỉ Admin).
- [ ] CRUD API `Course` (Quản lý các môn học cơ bản, số tín chỉ).
- [ ] Endpoint lấy danh sách Môn học đang mở trong một Học kỳ cụ thể.

## API: Quản lý Lớp học (Class)
- [ ] `POST /api/academic/classes`: Tạo lớp học, gắn vào Môn học và Học kỳ.
- [ ] `PUT /api/academic/classes/{id}/teacher`: API Gán/Thay đổi Giáo viên chủ nhiệm cho lớp.
- [ ] `POST /api/academic/classes/{id}/students`: API Thêm danh sách Học sinh vào Lớp (Lưu bảng `class_students`).

## API: Thời khóa biểu (Schedule)
- [ ] `POST /api/academic/classes/{id}/schedules`: Tạo lịch học (VD: Thứ 2, Tiết 1-3, Phòng 101).
- [ ] `GET /api/academic/schedules/my-schedule`: Trả về thời khóa biểu tuần của Giáo viên hoặc Học sinh đang đăng nhập.
