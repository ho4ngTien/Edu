# Frontend Checklist: Module 05 - Grade

**Trạng thái:** ⏳ Pending
**Tiến độ:** 0%

## Giao diện Giáo viên (Nhập điểm)
- [ ] Dựng Data Grid đặc biệt: Các ô điểm (Cell) có thể click vào và gõ trực tiếp (Inline Editing) giống hệt Excel.
- [ ] Sử dụng React Hook Form kết hợp với `useFieldArray` để quản lý mảng điểm số của cả lớp, tránh Re-render toàn bộ bảng khi chỉ gõ 1 ô.
- [ ] Logic lưu nháp (Save Draft) gửi API `bulk update` mỗi 30 giây hoặc có nút Lưu thủ công.
- [ ] Hiển thị cảnh báo màu đỏ nếu nhập sai định dạng (VD: > 10).

## Giao diện Học sinh (Báo cáo điểm)
- [ ] Màn hình Bảng điểm: Liệt kê các môn học và điểm thành phần.
- [ ] Áp dụng màu sắc (Color Coding) cho GPA: Xanh (Giỏi), Vàng (Khá), Đỏ (Yếu).
- [ ] (Nâng cao) Vẽ biểu đồ Radar hoặc Bar chart so sánh điểm số với trung bình lớp.
