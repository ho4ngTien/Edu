# Frontend Checklist: Module 01 - Authentication (Auth)

**Trạng thái:** ⏳ Pending
**Tiến độ:** 0%

## Chuẩn bị Cơ sở Hạ tầng
- [ ] Cấu hình Axios Interceptors: Tự động đính kèm Token vào Header `Authorization` cho mọi request.
- [ ] Xử lý interceptor lỗi 401: Tự động gọi API Refresh Token khi Access Token hết hạn, nếu thất bại thì văng ra trang Login.
- [ ] Khởi tạo `AuthContext` (React Context) để chứa Global State: `isAuthenticated`, `user (role, name)`, `login()`, `logout()`.
- [ ] Setup `react-hook-form` và `zod` để validate form nhanh, mượt.

## Thiết kế Màn hình Đăng nhập (UI)
- [ ] Khởi tạo Layout `app/(auth)/login/layout.tsx` (Chỉ chứa nền login, không sidebar).
- [ ] Code UI trang `login/page.tsx`: Nền Gradient, Glassmorphism Card (kính mờ).
- [ ] Tích hợp 2 ô input: Email, Password (có icon mắt hiển thị/ẩn password).
- [ ] Gắn Validation: Cảnh báo email sai định dạng, password không được để trống (Hiển thị chữ đỏ ngay dưới input).
- [ ] Hiển thị trạng thái Loading (Spinner) trên nút bấm khi đang gọi API.
- [ ] Hiển thị thông báo Toast (Thành công / Thất bại).

## Bảo mật Điều hướng (Route Guard)
- [ ] Xây dựng Component `ProtectedRoute`: Bọc quanh các Router của hệ thống.
- [ ] Nếu truy cập nhánh `/admin/*` mà chưa có Token -> Redirect về `/login`.
- [ ] Tự động chuyển hướng (Redirect) ngay lập tức sau khi đăng nhập thành công dựa vào Role (VD: Admin -> `/admin`, Học sinh -> `/courses`).
- [ ] Cấm người dùng đã đăng nhập quay lại màn hình `/login` (Tự đá về Dashboard).
