# Backend Checklist: Module 02 - User & Admin

**Trạng thái:** ⏳ Pending
**Tiến độ:** 0%

## Domain Entities & Repositories
- [ ] Xây dựng Entity `Profile` (Mối quan hệ 1-1 với AuthUser, qua UUID).
- [ ] Xây dựng Entity `Student` (Liên kết với Profile, có mã sinh viên).
- [ ] Xây dựng Entity `Teacher` (Liên kết với Profile, có mã giáo viên).
- [ ] Tạo các Repositories: `ProfileRepository`, `StudentRepository`, `TeacherRepository`.

## API: Hồ sơ Cá nhân (Profile)
- [ ] `GET /api/users/profile/me`: Lấy thông tin cá nhân (FullName, Avatar).
- [ ] `PUT /api/users/profile/me`: Cập nhật thông tin cơ bản.
- [ ] `POST /api/users/profile/avatar`: Tải lên và thay đổi ảnh đại diện (Tích hợp xử lý file multipart).

## API: Quản trị Học sinh (Admin -> Student)
- [ ] `POST /api/admin/students`: Khởi tạo tài khoản và hồ sơ học sinh.
- [ ] `GET /api/admin/students`: Liệt kê danh sách học sinh (Có phân trang, Search, Sort).
- [ ] `PUT /api/admin/students/{id}`: Chỉnh sửa thông tin học sinh.
- [ ] `DELETE /api/admin/students/{id}`: Xóa hoặc vô hiệu hóa tài khoản học sinh.

## API: Quản trị Giáo viên (Admin -> Teacher)
- [ ] `POST /api/admin/teachers`: Khởi tạo tài khoản và hồ sơ giáo viên.
- [ ] `GET /api/admin/teachers`: Liệt kê danh sách giáo viên.
- [ ] `PUT /api/admin/teachers/{id}`: Chỉnh sửa thông tin giáo viên.
- [ ] `DELETE /api/admin/teachers/{id}`: Xóa hoặc vô hiệu hóa tài khoản giáo viên.

## Bảo mật Module
- [ ] Gắn annotation `@PreAuthorize("hasRole('ADMIN')")` cho toàn bộ các API bắt đầu bằng `/api/admin`.
