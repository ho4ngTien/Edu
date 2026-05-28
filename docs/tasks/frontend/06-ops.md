# Frontend Checklist: Attendance & Notification Modules

**Trạng thái:** ⏳ Pending
**Tiến độ:** 0%

## Giao diện Điểm danh (Attendance)
- [ ] Màn hình Điểm danh: Danh sách học sinh kèm theo các Nút Radio hoặc Checkbox (Có mặt, Vắng, Có phép).
- [ ] Tính năng "Chọn tất cả là Có mặt" (Mark all as present) để tiết kiệm thời gian cho Giáo viên.

## Giao diện Thông báo (Notification)
- [ ] Xây dựng Component `NotificationBell` gắn trên thanh Navbar.
- [ ] Dropdown hiển thị 5 thông báo mới nhất. Tích hợp thanh cuộn vô tận (Infinite Scroll) nếu kéo xuống xem thông báo cũ.
- [ ] Nhấn vào thông báo nào, gọi API đánh dấu đã đọc (`isRead = true`) và giảm số đếm màu đỏ trên cái chuông.
- [ ] Dùng `setInterval` gọi API 1 phút 1 lần (Polling) để kiểm tra có thông báo mới không.
