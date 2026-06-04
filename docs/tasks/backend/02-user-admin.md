# Backend Checklist: Module 02 - User & Admin

**Trạng thái:** ✅ Done  
**Tiến độ:** 100%

> Hướng dẫn agent: `skills/backend-m02-user-admin/SKILL.md`

---

## Domain Entities & Repositories

- [x] Xây dựng Entity `Profile` (Mối quan hệ 1-1 với AuthUser, qua UUID `authUserId`).
- [x] Xây dựng Entity `Student` (Liên kết với Profile, có mã sinh viên).
- [x] Xây dựng Entity `Teacher` (Liên kết với Profile, có mã giáo viên).
- [x] Tạo các Repositories: `ProfileRepository`, `StudentRepository`, `TeacherRepository`.
- [x] Entity `Staff` + `StaffRepository` (mở rộng nhân sự).

## API: Hồ sơ Cá nhân (Profile)

- [x] `GET /api/users/profile/me`: Lấy thông tin cá nhân (FullName, Avatar).
- [x] `PUT /api/users/profile/me`: Cập nhật thông tin cơ bản.
- [x] `POST /api/users/profile/avatar`: Upload ảnh đại diện (Multipart, lưu `uploads/avatars/`).

## API: Quản trị Học sinh (Admin → Student)

- [x] `POST /api/admin/students`: Khởi tạo tài khoản Auth + Profile + Student.
- [x] `GET /api/admin/students`: Danh sách có phân trang + `?search=`.
- [x] `GET /api/admin/students/{id}`: Chi tiết học sinh.
- [x] `PUT /api/admin/students/{id}`: Cập nhật thông tin.
- [x] `DELETE /api/admin/students/{id}`: Vô hiệu hóa (`isActive = false`).

## API: Quản trị Giáo viên (Admin → Teacher)

- [x] `POST /api/admin/teachers`: Khởi tạo tài khoản Auth + Profile + Teacher.
- [x] `GET /api/admin/teachers`: Danh sách có phân trang + `?search=`.
- [x] `GET /api/admin/teachers/{id}`: Chi tiết giáo viên.
- [x] `PUT /api/admin/teachers/{id}`: Cập nhật thông tin.
- [x] `DELETE /api/admin/teachers/{id}`: Vô hiệu hóa.

## Bảo mật Module

- [x] `@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")` trên API `/api/admin/students` và `/api/admin/teachers`.

## File tham chiếu

| Thành phần | Đường dẫn |
|------------|-----------|
| User API | `modules/user/controller/UserController.java` |
| Admin CRUD | `modules/admin/controller/AdminController.java` |
| Admin user service | `modules/admin/service/AdminUserManagementService.java` |
| ERD | `docs/erd/db_user.dbml` |
