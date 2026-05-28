# Frontend Checklist: Module 03 - Academic Core

**Trạng thái:** ⏳ Pending
**Tiến độ:** 0%

## Quản trị Viên (Admin Views)
- [ ] Màn hình `Danh mục Học kỳ` (Thêm/Sửa/Kích hoạt học kỳ hiện tại).
- [ ] Màn hình `Danh mục Môn học` (Quản lý thư viện môn học).
- [ ] Giao diện **Xếp Lớp học**: Một màn hình có 2 cột. Trái là danh sách Học sinh chưa có lớp, Phải là Lớp học đang chọn. Dùng Component Danh sách chọn hoặc Drag & Drop.
- [ ] Giao diện xếp Lịch: Form nhập nhanh chọn Thứ, Tiết, Phòng cho lớp.

## Giáo viên (Teacher Views)
- [ ] Trang `/teacher/classes`: Hiển thị dạng thẻ (Card) các lớp học mà Giáo viên đang được phân công giảng dạy.
- [ ] Màn hình `Chi tiết Lớp học`: Xem danh sách học sinh trong lớp (Chỉ xem, không được xóa học sinh khỏi lớp, quyền đó của Admin).

## Thời khóa biểu (Calendar UI)
- [ ] Tích hợp thư viện `react-big-calendar` hoặc `fullcalendar` (hoặc tự code Grid CSS).
- [ ] Hiển thị Thời khóa biểu dạng Tuần (Weekly View) cho cả Giáo viên và Học sinh dựa trên API `/my-schedule`.
