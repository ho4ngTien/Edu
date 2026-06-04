# Lộ trình Phát triển Tổng thể (Detailed Module-by-Module Roadmap)

Tài liệu này trình bày chi tiết các chức năng (Features) sẽ được triển khai cho từng Module (Fullstack) từ Backend đến Frontend.

---

## M01: Authentication Module (`auth`)
**Mục tiêu:** Quản lý truy cập và bảo vệ toàn bộ hệ thống.
- **Backend Features:**
  - `POST /api/auth/login`: Xác thực Username/Password, trả về Access Token (15 phút) và Refresh Token (7 ngày).
  - `POST /api/auth/refresh`: Dùng Refresh Token để xin cấp lại Access Token mới (cơ chế UPSERT chống race-condition).
  - `POST /api/auth/logout`: Xóa Refresh Token khỏi DB.
  - Bộ lọc `JwtAuthenticationFilter`: Phân tích Token, gán Role (ADMIN, TEACHER, STUDENT) vào `SecurityContext`.
- **Frontend Features:**
  - Giao diện Login Glassmorphism, Responsive (chạy tốt trên Mobile/Desktop).
  - Quản lý State bằng Context API (Lưu thông tin User đang đăng nhập).
  - Xử lý hết hạn Token (Tự động gọi API Refresh ngầm hoặc văng ra màn hình đăng nhập).
  - `ProtectedRoute`: Tự động điều hướng dựa trên Role (Học sinh cố tình vào link Admin sẽ bị chặn).

---

## M02: User & Admin Module (`user`, `admin`)
**Mục tiêu:** Quản lý nhân sự, học sinh, giáo viên.

**Tiến độ Backend:** ✅ Done — [tasks/backend/02-user-admin.md](backend/02-user-admin.md).
- **Backend Features:**
  - Quản lý `Profile` (Upload Avatar, Đổi mật khẩu, Sửa thông tin cá nhân).
  - Admin CRUD `Student` (Mã số học sinh, Ngày nhập học).
  - Admin CRUD `Teacher` (Mã giáo viên, Khoa/Bộ môn).
  - Cấp phát và thu hồi quyền (`Role` & `Permission`).
- **Frontend Features:**
  - **Admin Dashboard:** Tổng quan số lượng Học sinh/Giáo viên/Lớp học.
  - **Bảng Data Grid:** Hiển thị danh sách Người dùng có tích hợp Tìm kiếm (Search), Lọc (Filter), và Phân trang (Pagination).
  - Form Thêm mới / Cập nhật Học sinh, Giáo viên (có Validation).

---

## M03: Academic Module (`academic`)
**Mục tiêu:** Quản lý khung chương trình đào tạo.
- **Backend Features:**
  - CRUD `Semester` (Học kỳ: Fall 2026, Spring 2027...).
  - CRUD `Course` (Môn học: Toán Cao cấp, Lập trình Java...).
  - CRUD `Class` (Lớp học: K65-CS1, K65-CS2...). Phân công Giáo viên chủ nhiệm.
  - API tạo Thời khóa biểu (Schedule): Thứ mấy, tiết mấy, phòng nào.
- **Frontend Features:**
  - Màn hình quản lý Danh mục Môn học, Học kỳ.
  - Giao diện Kéo/Thả (Drag & Drop) hoặc Form để xếp Lớp và Thời khóa biểu.
  - Giáo viên xem danh sách các Lớp mình đang giảng dạy.

---

## M04: LMS Module (`lms`)
**Mục tiêu:** Không gian Dạy & Học trực tuyến (Learning Management System).
- **Backend Features:**
  - CRUD `Lesson` (Bài giảng thuộc về Lớp học).
  - Upload `Material` (Tài liệu học tập, PDF, Slide đính kèm bài giảng).
  - Tạo `Assignment` (Bài tập về nhà, Hạn chót - Deadline).
  - Nộp bài `Submission` (Học sinh nộp File hoặc Text).
- **Frontend Features:**
  - **Course Focus Mode:** Giao diện xem bài giảng dạng chia đôi màn hình (Trái: Menu bài giảng, Phải: Video/Nội dung), ẩn toàn bộ Sidebar để học sinh tập trung.
  - Khu vực tải tài liệu và nộp bài tập (Upload File UI).
  - Giáo viên có màn hình xem danh sách học sinh đã/chưa nộp bài.

---

## M05: Grade Module (`grade`)
**Mục tiêu:** Chấm điểm, đánh giá và xuất báo cáo.
- **Backend Features:**
  - Định nghĩa `Grade Category` (Điểm chuyên cần, Bài tập, Giữa kỳ, Cuối kỳ).
  - Nhập điểm (Bulk Insert) cho cả lớp.
  - API tính Điểm Trung Bình (GPA) tự động.
- **Frontend Features:**
  - **Giao diện Lưới nhập điểm:** Giống Excel, cho phép Giáo viên nhập điểm nhanh cho nhiều học sinh cùng lúc.
  - **Bảng điểm Cá nhân:** Học sinh xem biểu đồ điểm số, cảnh báo môn học sắp trượt.

---

## M06: Attendance Module (`attendance`)
**Mục tiêu:** Theo dõi chuyên cần, đánh dấu điểm danh hàng ngày/từng tiết học.
- **Backend Features:**
  - API Điểm danh (`POST /api/attendance/classes/{classId}`): Có mặt, Vắng phép, Vắng không phép.
- **Frontend Features:**
  - Form điểm danh dạng Checkbox nhanh cho Giáo viên.

---

## M07: Notification Module (`notification`)
**Mục tiêu:** Trung tâm gửi thông báo: In-app, Emails, Deadline Reminders.
- **Backend Features:**
  - Hệ thống `Spring Application Events` (Phát sóng thông báo khi: Có bài tập mới, Có điểm mới, Lịch học thay đổi...).
  - API lấy thông báo cá nhân (`GET /api/notifications/my-notifications`) và đánh dấu đã đọc (`PUT /api/notifications/{id}/read`).
  - (Tùy chọn tương lai) Gửi thông báo qua Email/SMS thực tế.
- **Frontend Features:**
  - Cột chuông thông báo (Notification Dropdown) trên thanh Navbar để hiện thông báo Realtime cho người dùng.

---

**File Tracking Rules:**
Khi tiến hành code Module nào, một file task cụ thể (VD: `tasks/modules/01-auth.md`) sẽ được cập nhật để đánh dấu tiến độ (Checklist `[x]`). Điều này giúp theo dõi dự án sát sao ở cấp độ Component và API.
