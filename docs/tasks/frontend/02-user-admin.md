# Frontend Checklist: Module 02 - User & Admin

**Trạng thái:** ⏳ Pending
**Tiến độ:** 0%

## Component Tái sử dụng (Shared UI)
- [ ] Xây dựng Component `DataGrid` (Table) có chức năng: Sắp xếp cột, Phân trang (Pagination), Loading Skeleton.
- [ ] Xây dựng Component `Modal` để mở các form Thêm/Sửa mà không cần chuyển trang.
- [ ] Thiết kế `ProfileAvatar` (Component nhỏ hiển thị ảnh đại diện và tên viết tắt).

## Layout & Trang Cá nhân (Profile)
- [ ] Xây dựng `Navbar` có menu Dropdown (Trang cá nhân, Đăng xuất).
- [ ] Layout trang Cá nhân `/profile`: Cho phép đổi tên, cập nhật số điện thoại.
- [ ] Tích hợp React Dropzone để xử lý Upload Avatar lên Cloud / Server.

## Admin: Quản lý Học sinh
- [ ] Màn hình `/admin/students`: Khai báo bảng hiển thị danh sách Học sinh.
- [ ] Viết hook `useStudents` (Dùng React Query / SWR) để call API GET và cache dữ liệu.
- [ ] Popup Form Thêm Học sinh: Nhập Email, Tên, Mã số, Mật khẩu. Validate bằng Zod.
- [ ] Nút "Xóa/Khóa" (Kèm Modal xác nhận nguy hiểm `ConfirmDialog`).

## Admin: Quản lý Giáo viên
- [ ] Màn hình `/admin/teachers`: Bảng hiển thị danh sách Giáo viên.
- [ ] Popup Form Thêm Giáo viên: Nhập Khoa, Chuyên môn.
- [ ] Đồng bộ hóa dữ liệu (Refetch) ngay lập tức sau khi có hành động Thêm/Sửa/Xóa thành công.
