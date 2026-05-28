# Luồng Nghiệp vụ & Các tình huống sử dụng (Business Flows) - SMS

Tài liệu này mô tả cách các tác nhân (Actors) tương tác với Hệ thống Quản lý Trường học (SMS) thông qua 3 Portals (Student, Teacher, Admin) và cách các Modules ở Backend phối hợp để xử lý dữ liệu.

## 1. Tác nhân trong Hệ thống (Actors)

- **Admin (Ban Giám Hiệu/Nhân sự):** Quản lý toàn bộ dữ liệu cấu trúc (Năm học, Lớp học, Tài khoản) qua Admin Portal.
- **Teacher (Giáo viên):** Giảng dạy, giao bài, nhập điểm và điểm danh qua Teacher Portal.
- **Student (Học sinh):** Xem lịch học, nộp bài, xem điểm qua Student Portal.
- **System (Hệ thống):** Xử lý các batch jobs (nhắc deadline, gửi báo cáo).

---

## 2. Luồng Nghiệp vụ Cốt lõi (Core Flows)

### 2.1. Luồng Khởi tạo Năm học & Xếp lớp (Bởi Admin)
*Sự phối hợp giữa: User Module, Academic Module.*

1. Khởi tạo dữ liệu:
   - Admin vào `Admin Portal`, tạo **Năm học mới** và **Học kỳ** (gọi API tới `Academic Module`).
   - Cập nhật danh mục **Môn học** nếu có thay đổi.
2. Tạo Lớp và Phân công:
   - Tạo các **Lớp học** (Ví dụ: 10A1, 10A2) trong `Academic Module`.
   - Phân công **Giáo viên chủ nhiệm** (Lấy danh sách giáo viên từ `User Module`).
   - Gán **Học sinh** vào lớp (Tạo bảng ghi Enrollments trong `Academic Module` hoặc `Operations Module`).
3. Tạo Thời khóa biểu:
   - Admin/Giáo vụ xếp lịch học cho từng lớp (Gán môn học, giáo viên, phòng học vào các tiết trong tuần).

### 2.2. Luồng Giảng dạy & Đánh giá (Bởi Teacher & Student)
*Sự phối hợp giữa: LMS Module, Grade Module, Notification Module.*

1. Tùy chỉnh môn học và Giao bài (Teacher):
   - Giáo viên vào `Teacher Portal`, truy cập `/teacher/subjects/:subjectId` để quản lý chuyên sâu môn học.
   - Tải lên tài liệu mới hoặc tạo **Bài tập (Assignment)** (Gọi API tới `LMS Module`).
   - `Notification Module` nhận event từ LMS, gửi email/thông báo nhắc nhở đến các học sinh trong lớp.
2. Học tập ở chế độ Tập trung (Course Focus Mode):
   - Học sinh (hoặc giáo viên xem trước) truy cập vào `/courses/:courseId` (sử dụng `CourseLayout` tách biệt không có sidebar rườm rà).
   - Xem tài liệu bài giảng, video một cách tập trung nhất.
3. Nộp bài (Student):
   - Học sinh vào `Student Portal`, truy cập `/student/assignments` để xem danh sách bài tập.
   - Upload file / Viết text nộp bài. (`LMS Module` lưu **Submission**).
4. Chấm điểm (Teacher):
   - Giáo viên xem các Submission.
   - Nhập điểm và lời phê. (`Grade Module` nhận dữ liệu điểm).
   - Hệ thống tự động đẩy thông báo cho học sinh là đã có điểm qua `Notification Module`.

### 2.3. Luồng Điểm danh Hằng ngày (Bởi Teacher)
*Sự phối hợp giữa: Academic Module, Attendance Module.*

1. Đầu tiết học, Giáo viên mở màn hình điểm danh trên `Teacher Portal`.
2. Hệ thống gọi `Academic Module` để lấy danh sách học sinh của lớp học trong tiết đó.
3. Giáo viên đánh dấu (PRESENT, ABSENT...). 
4. Hệ thống gọi `Attendance Module` để lưu dữ liệu điểm danh. Nếu học sinh vắng mặt không phép, một event có thể kích hoạt `Notification Module` báo SMS/Email về cho phụ huynh (Parent).

### 2.4. Luồng Tổng kết Cuối kỳ (Bởi System & Admin)
*Sự phối hợp giữa: Grade Module, Admin Module.*

1. Khi kết thúc học kỳ, `Grade Module` tổng hợp toàn bộ điểm số của các bài kiểm tra (tính theo hệ số từ `Assessment module`).
2. Tính toán điểm trung bình (GPA) cho từng học sinh.
3. `Admin Module` hoặc hệ thống Job tự động sẽ khóa sổ điểm, chốt **Bảng điểm (Transcript)**.
4. Trích xuất báo cáo học thuật toàn trường.
