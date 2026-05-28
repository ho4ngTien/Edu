# Backend Checklist: Module 05 - Grade

**Trạng thái:** ⏳ Pending
**Tiến độ:** 0%

## API: Danh mục Điểm (Grade Category)
- [ ] Thiết lập bảng trọng số điểm (Chuyên cần: 10%, Giữa kỳ: 30%, Cuối kỳ: 60%).
- [ ] Cho phép Giáo viên điều chỉnh cấu trúc điểm cho từng lớp học.

## API: Nhập & Xử lý Điểm (Grading)
- [ ] `POST /api/grade/classes/{classId}/bulk`: API nhận mảng dữ liệu điểm của nhiều học sinh cùng lúc (Phục vụ UI nhập điểm như Excel).
- [ ] Xây dựng thuật toán tự động tính Điểm Tổng Kết (GPA) ngay khi giáo viên lưu điểm thành phần.
- [ ] Validate chặn nhập điểm < 0 hoặc > 10.

## API: Bảng điểm (Transcript)
- [ ] `GET /api/grade/students/{studentId}/transcript`: Trả về toàn bộ bảng điểm của một học sinh trong một học kỳ.
- [ ] Khóa điểm (Lock): Tính năng chốt điểm cuối kỳ, không cho phép sửa đổi sau khi đã chốt.
