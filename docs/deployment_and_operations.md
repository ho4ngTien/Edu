# Vận hành, Giám sát & Triển khai - EduIDE

Tài liệu này hướng dẫn cách cấu hình môi trường, thiết lập khởi chạy cục bộ, triển khai bằng Docker và quản lý tài nguyên biên dịch động của dự án **EduIDE**.

---

## 1. Yêu cầu Hệ thống tối thiểu (Prerequisites)

Để cài đặt và chạy ứng dụng cục bộ thành công, máy tính của lập trình viên cần đáp ứng:
* **Hệ điều hành:** Windows 10/11, macOS hoặc Linux.
* **Java Development Kit (JDK):** Phiên bản **21** trở lên (Đã được cấu hình trong `pom.xml` của backend).
* **Node.js:** Phiên bản **18.x** trở lên kèm trình quản lý gói `npm`.
* **Maven:** Bản Maven tích hợp sẵn (hoặc cài ngoài) để chạy backend Spring Boot.
* **Docker & Docker Compose:** (Không bắt buộc) Nếu muốn đóng gói và chạy ứng dụng qua môi trường ảo hóa Container.

---

## 2. Các Kịch bản Khởi chạy nhanh (Startup Scripts)

Hệ thống cung cấp sẵn các kịch bản chạy tự động trong thư mục `infrastructure/` giúp khởi chạy monorepo chỉ với một cú nhấp chuột.

### A. Chạy ở môi trường phát triển cục bộ (Development Mode)
Khuyến khích sử dụng tệp `infrastructure/run-dev.bat` để chạy ứng dụng trong quá trình lập trình:

1. Mở cửa sổ dòng lệnh (Terminal) và điều hướng đến thư mục `infrastructure/`.
2. Chạy lệnh: `run-dev.bat`.
3. **Cơ chế hoạt động của kịch bản:**
   * Mở một cửa sổ terminal mới để chạy Backend Spring Boot (`mvn spring-boot:run`) tại cổng `8080`.
   * Mở một cửa sổ terminal khác để chạy Frontend React Vite (`npm run dev`) tại cổng `5173`.
   * Chờ 5 giây cho các dịch vụ khởi động ổn định rồi tự động mở trình duyệt truy cập địa chỉ: `http://localhost:5173`.

### B. Chạy thông qua Docker Compose (Production/Docker Mode)
Hệ thống hỗ trợ đóng gói cô lập toàn bộ Front-end và Back-end qua Docker thông qua tệp `infrastructure/docker-compose.yml` và kịch bản `run-docker.bat`:

1. Khởi động ứng dụng Docker Desktop trên máy tính.
2. Chạy lệnh: `run-docker.bat` (hoặc lệnh `docker-compose -f infrastructure/docker-compose.yml up --build -d`).
3. **Cơ chế phân bổ dịch vụ:**
   * Dịch vụ **`edu-backend`**: Build dựa trên `backend/Dockerfile`, mở cổng kết nối `8080:8080`.
   * Dịch vụ **`edu-frontend`**: Build dựa trên `frontend/Dockerfile`, ánh xạ cổng `80:80` (HTTP chuẩn) và cổng dự phòng `5173:80` ra ngoài máy chủ vật lý.
   * Cả hai giao tiếp an toàn thông qua mạng cầu nội bộ mang tên `edu-network`.

---

## 3. Nhật ký Vận hành & Cấu hình Gỡ lỗi (Logging & Debugging)

### A. Cấu hình mức độ log tại Backend
Mức độ log chi tiết của hệ thống được cấu hình trong tệp `backend/src/main/resources/application.properties`:
```properties
# Logging Configurations
logging.level.com.edu.ide=DEBUG
logging.level.org.springframework.web=INFO
```
* **`DEBUG` cho `com.edu.ide`:** Hiển thị chi tiết nhật ký biên dịch mã nguồn Java, thông tin ghi/xóa tệp trong Workspace, mã UUID thư mục tạm thời được cấp và các tiến trình con được khởi động giúp dễ dàng phát hiện lỗi bảo mật hoặc tắc nghẽn tiến trình.
* **`INFO` cho `org.springframework.web`:** Chỉ hiển thị tóm tắt thông tin các yêu cầu REST API gửi lên, tránh gây loãng thông tin log.

### B. Nhật ký lỗi hiển thị cho Học sinh (Client-side Terminal Logs)
Mọi phản hồi từ trình biên dịch và hệ thống đều được định tuyến về React Context (`IdeContext.tsx`) và hiển thị trực tiếp lên bảng điều khiển Console của IDE với các nhãn phân biệt màu trực quan:
* **`[HỆ THỐNG]` (Màu xanh dương - Info / Màu xanh lá - Success):** Thông báo trạng thái hệ thống tệp, chào mừng người dùng, hoặc thông báo hoàn thành nhiệm vụ thành công.
* **`[BIÊN DỊCH]` (Màu xám/vàng - Warning):** Thông báo tệp tin đang được chuẩn bị nộp và gửi lên máy chủ để biên dịch.
* **`[LỖI BIÊN DỊCH]` (Màu đỏ - Error):** In chi tiết log lỗi cú pháp của trình biên dịch `javac` trả về, chỉ rõ số dòng và nguyên nhân lỗi để học sinh sửa mã nguồn trực tiếp trong Editor.
* **`[LỖI HỆ THỐNG]` (Màu đỏ - Error):** In lỗi khi gặp sự cố đĩa cứng hoặc vi phạm bảo mật (ví dụ: phát hiện Directory Traversal).

---

## 4. Quản lý Tài nguyên đĩa cứng (Compilation Resource Management)

Để tránh trường hợp hàng ngàn tệp tin tạm `.java` và `.class` tích tụ làm cạn kiệt dung lượng đĩa cứng của máy chủ, mô-đun thực thi `execution` áp dụng giải pháp dọn dẹp đệ quy nghiêm ngặt thông qua cấu trúc `try-finally`:

```java
try {
    // 1. Tạo thư mục tạm temp_compile/[UUID]
    // 2. Ghi mã và chạy biên dịch/thực thi
    ...
} finally {
    // 3. Gọi hàm cleanup dọn dẹp đệ quy toàn bộ thư mục tạm và tệp con
    cleanup(tempDir.toFile());
}
```

**Cơ chế hoạt động của hàm `cleanup`:**
* Quét toàn bộ thư mục con và tệp tin nằm dưới `temp_compile/[UUID]`.
* Thực hiện xóa từ dưới lên (xóa các tệp `.class`, `.java` trước, sau đó xóa thư mục UUID).
* Việc đóng gói khối dọn dẹp trong mệnh đề `finally` đảm bảo rằng dù chương trình của học sinh biên dịch thành công, thất bại, gặp lỗi thời gian chạy (Runtime Exception) hay tiến trình bị cưỡng chế dừng do quá giờ (Timeout), đĩa cứng máy chủ vẫn được giải phóng hoàn toàn ngay lập tức.
