# Vận hành, Giám sát & Triển khai - School Management System (SMS)

Tài liệu này hướng dẫn cách cấu hình môi trường, thiết lập khởi chạy cục bộ và triển khai hệ thống quản lý trường học **SMS (Modular Monolith)** bằng Docker.

---

## 1. Yêu cầu Hệ thống tối thiểu (Prerequisites)

Để cài đặt và chạy ứng dụng cục bộ thành công, máy tính cần đáp ứng:
* **Hệ điều hành:** Windows 10/11, macOS hoặc Linux.
* **Java Development Kit (JDK):** Phiên bản **21** trở lên.
* **Node.js:** Phiên bản **20.x** trở lên kèm trình quản lý gói `npm`.
* **Cơ sở dữ liệu:** PostgreSQL 15+ (Có thể chạy qua Docker).
* **Docker & Docker Compose:** Yêu cầu bắt buộc nếu muốn chạy trọn gói (Front-end, Back-end, Database) chỉ với 1 lệnh.

---

## 2. Các Kịch bản Khởi chạy nhanh (Startup Scripts)

### A. Chạy thông qua Docker Compose (Khuyên dùng - Fullstack)
Hệ thống hỗ trợ đóng gói cô lập toàn bộ Database, Front-end và Back-end qua Docker thông qua tệp `infrastructure/docker-compose.yml`:

1. Bật Docker Desktop trên máy tính.
2. Mở terminal tại thư mục `infrastructure/`.
3. Chạy lệnh: `docker-compose up --build -d` (hoặc click đúp vào `run-docker.bat`).
4. **Cơ chế phân bổ dịch vụ:**
   * **`edu-postgres`**: Database PostgreSQL lưu trữ toàn bộ dữ liệu (Port 5432).
   * **`edu-backend`**: Máy chủ Spring Boot (Port 8080), tự động kết nối tới DB.
   * **`edu-frontend`**: Giao diện React/Vite (Port 80 HTTP chuẩn hoặc 5173).

### B. Chạy ở môi trường phát triển cục bộ (Development Mode)
Dành cho lập trình viên muốn code và thấy thay đổi ngay lập tức (Hot Reload):

1. **Khởi động Database:** (Bắt buộc) `docker-compose up -d postgres`
2. **Khởi động Backend:** 
   * Mở terminal tại thư mục `backend/`
   * Chạy lệnh: `mvn spring-boot:run`
3. **Khởi động Frontend:**
   * Mở terminal tại thư mục `frontend/`
   * Chạy lệnh: `npm install` (lần đầu)
   * Chạy lệnh: `npm run dev`

---

## 3. Quản lý Môi trường & Biến (Environment Variables)

### Backend (`application.properties`)
- `spring.datasource.url`: Chuỗi kết nối PostgreSQL (`jdbc:postgresql://localhost:5432/sms` khi chạy dev, hoặc `jdbc:postgresql://postgres:5432/sms` khi chạy Docker).
- `jwt.secret`: Khóa bí mật dùng để mã hóa Access Token (Bắt buộc phải đổi thành chuỗi ngẫu nhiên 256-bit trên Production).
- `spring.jpa.hibernate.ddl-auto`: Để `update` trong lúc dev, bắt buộc chuyển thành `validate` trên Production.

### Frontend (`.env`)
- `VITE_API_BASE_URL`: Địa chỉ của Backend (VD: `http://localhost:8080`).

---

## 4. Nhật ký Vận hành (Logging & Debugging)

Mức độ log chi tiết của hệ thống được cấu hình trong `application.properties`:
```properties
logging.level.com.edu.sms=DEBUG
logging.level.org.springframework.web=INFO
logging.level.org.hibernate.SQL=DEBUG
```
* **`DEBUG` cho `com.edu.sms`**: Theo dõi chi tiết luồng xử lý của các Module (Auth, User, Academic...).
* **`DEBUG` cho `SQL`**: Kiểm tra các câu truy vấn thực tế được JPA sinh ra để tối ưu hóa hiệu năng (N+1 query problem).
* Các lỗi (Exceptions) sẽ được Gom tụ lại xử lý tại `GlobalExceptionHandler` và trả về mã HTTP chuẩn (400, 401, 403, 404, 500) kèm message rõ ràng cho Frontend.
