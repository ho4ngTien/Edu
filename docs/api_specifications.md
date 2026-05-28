# Tài liệu Đặc tả REST API - School Management System (SMS)

Tài liệu này cung cấp cái nhìn tổng quan về cấu trúc các API Endpoint nghiệp vụ dùng để kết nối giữa Frontend (React/Vite) và Backend (Spring Boot) trong hệ thống **SMS (Modular Monolith)**.

---

## 1. Cấu hình Giao tiếp API Chung (General Specifications)

* **Base URI:** `/api` (Mặc định máy chủ chạy tại cổng `8080`, cung cấp các endpoint qua tiền tố `/api/...`).
* **CORS Policy:** Tất cả các endpoint đều hỗ trợ CORS để Frontend (ví dụ: `http://localhost:5173`) có thể giao tiếp không bị lỗi.
* **Định dạng dữ liệu:** Mọi dữ liệu yêu cầu (Request Body) và phản hồi (Response Body) đều tuân thủ định dạng chuẩn **JSON (Application/JSON)**.
* **Xác thực (Authentication):** Các API được bảo vệ yêu cầu Header: `Authorization: Bearer <Access_Token>`.

---

## 2. Danh mục API Endpoints Chi tiết theo Module

*(Lưu ý: Đây là tài liệu quy hoạch đường dẫn API. Chi tiết về Payload Request/Response sẽ được định nghĩa cụ thể trong quá trình code từng Module).*

### M01: Module Authentication (`/api/auth`)
* `POST /api/auth/login`: Xác thực Email/Password, trả về Access Token và Refresh Token.
* `POST /api/auth/refresh`: Dùng Refresh Token để xin cấp lại Access Token mới (cơ chế UPSERT).
* `POST /api/auth/logout`: Đăng xuất, hủy bỏ Refresh Token hiện tại.

### M02: Module User & Admin (`/api/users` & `/api/admin`)
* `GET /api/users/profile/me`: Lấy thông tin tài khoản đang đăng nhập.
* `PUT /api/users/profile/me`: Cập nhật thông tin cá nhân.
* `GET /api/admin/students`: Lấy danh sách toàn bộ học sinh (Chỉ Admin).
* `POST /api/admin/students`: Thêm mới học sinh.
* `GET /api/admin/teachers`: Lấy danh sách giáo viên.

### M03: Module Academic (`/api/academic`)
* `GET /api/academic/semesters`: Danh sách các Học kỳ.
* `GET /api/academic/courses`: Danh mục Môn học.
* `GET /api/academic/classes`: Danh sách Lớp học đang mở.
* `POST /api/academic/classes`: Mở lớp học mới.
* `GET /api/academic/schedules/my-schedule`: Lấy Thời khóa biểu cá nhân của Giáo viên/Học sinh.

### M04: Module LMS (`/api/lms`)
* `GET /api/lms/classes/{classId}/lessons`: Lấy cấu trúc bài giảng của một lớp.
* `POST /api/lms/lessons/{lessonId}/materials`: Upload tài liệu tham khảo.
* `POST /api/lms/lessons/{lessonId}/assignments`: Giao bài tập về nhà.
* `POST /api/lms/assignments/{assignmentId}/submit`: Học sinh upload bài làm (Submission).

### M05: Module Grade (`/api/grade`)
* `POST /api/grade/classes/{classId}/bulk`: Nhập điểm hàng loạt cho cả lớp.
* `GET /api/grade/students/{studentId}/transcript`: Lấy bảng điểm tổng kết (Transcript).

### M06: Module Attendance (`/api/attendance`)
* `POST /api/attendance/classes/{classId}`: Ghi nhận điểm danh hàng ngày.

### M07: Module Notification (`/api/notifications`)
* `GET /api/notifications/my-notifications`: Lấy danh sách thông báo chưa đọc.
* `PUT /api/notifications/{id}/read`: Đánh dấu thông báo đã đọc.

---

## 3. Cấu trúc Phản hồi Tiêu chuẩn (Standard Response Wrapper)

Mọi API trả về (kể cả lỗi) đều tuân thủ chung một cấu trúc `ApiResponse`:

```json
{
  "timestamp": "2026-05-27T00:41:00Z",
  "status": 200,
  "success": true,
  "message": "Đăng nhập thành công",
  "data": {
    "accessToken": "eyJhbG...",
    "refreshToken": "uuid-..."
  }
}
```

*Khi có lỗi (VD: 400 Bad Request, 404 Not Found), trường `success` sẽ là `false` và `data` sẽ chứa chi tiết lỗi (Ví dụ: Danh sách các trường Validation bị sai).*
