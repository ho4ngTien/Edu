# Backend Checklist: Attendance & Notification Modules

**Trạng thái:** ⏳ Pending
**Tiến độ:** 0%

## Điểm danh (Attendance)
- [ ] Thiết kế bảng `attendances` lưu trạng thái (PRESENT, ABSENT, EXCUSED).
- [ ] API Nhập điểm danh hàng ngày cho một lớp học (`POST /api/attendance/classes/{id}`).
- [ ] Logic cảnh báo: Học sinh nghỉ quá 20% số tiết sẽ tự động phát sinh sự kiện cảnh báo.

## Thông báo (Notification Engine)
- [ ] Thiết lập `ApplicationEventPublisher` của Spring để bắn Event nội bộ giữa các Module (Loose coupling).
  - VD: Module LMS giao bài tập xong -> bắn `AssignmentCreatedEvent` -> Module Notification bắt lấy và sinh ra Notification lưu vào DB.
- [ ] API `GET /api/notifications/my-notifications`: Trả về danh sách thông báo của User đang đăng nhập.
- [ ] API `PUT /api/notifications/{id}/read`: Đánh dấu thông báo đã đọc.
