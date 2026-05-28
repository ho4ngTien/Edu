# Kiến trúc & Thiết kế Hệ thống - School Management System (SMS)

Tài liệu này cung cấp cái nhìn chi tiết về kiến trúc phần mềm, nguyên tắc phân vùng, ranh giới dữ liệu trong hệ thống **School Management System (SMS)**.

---

## 1. Giới thiệu Kiến trúc Tổng quan (Architecture Overview)

Hệ thống được xây dựng dưới dạng **Modular Monolith Architecture**. 
1. **Frontend (Client-side):** Là một ứng dụng **SPA duy nhất (Single Page Application)** sử dụng React/Next.js. Thay vì tạo 3 ứng dụng riêng biệt, hệ thống sẽ điều hướng (Routing) dựa trên Role của người dùng sau khi Login thành công:
   - `/student/*`: Phân hệ dành cho học sinh.
   - `/teacher/*`: Phân hệ dành cho giáo viên.
   - `/admin/*`: Phân hệ dành cho nhân sự và ban giám hiệu.
   - `/courses/*`: Phân hệ trung lập dùng chung để xem nội dung khóa học/bài giảng ở chế độ tập trung.
   > **Lưu ý Kiến trúc UI:** Để đảm bảo khả năng mở rộng độc lập, **Layout của các phân hệ này được tách biệt hoàn toàn** (ví dụ: `AdminLayout`, `TeacherLayout`, `StudentLayout`, `CourseLayout`). Chúng ta không dùng chung Layout rập khuôn để tránh việc logic của Admin bị rối vào giao diện của Student, và CourseLayout sẽ ẩn đi các Sidebar điều hướng phức tạp để tăng cường tính tập trung.
2. **Backend (Server-side):** Là một ứng dụng Spring Boot duy nhất (Monolith) nhưng bên trong chia thành 8 Modules độc lập (tương ứng với 8 Bounded Contexts). Giao tiếp vào hệ thống đi qua các Controllers tập trung (hoặc 1 API Gateway mềm).
3. **Database:** PostgreSQL. Mỗi Module quản lý một Schema độc lập, đảm bảo nguyên tắc Data Isolation.

```mermaid
graph TD
    User([Người dùng]) --> Login[Màn hình Đăng Nhập]
    Login -->|Role: Student| Route_Student[/student/*]
    Login -->|Role: Teacher| Route_Teacher[/teacher/*]
    Login -->|Role: Admin| Route_Admin[/admin/*]
    
    User -->|Học tập| Route_Course[/courses/*]

    Route_Student --> Backend[Spring Boot Monolith Backend]
    Route_Teacher --> Backend
    Route_Admin --> Backend
    Route_Course --> Backend

    subgraph "Modular Monolith Backend"
        Auth[Auth Module]
        User[User Module]
        Academic[Academic Module]
        LMS[LMS Module]
        Grade[Grade Module]
        Attendance[Attendance Module]
        Notification[Notification Module]
        Admin[Admin Module]
    end

    Auth -.-> DB_Auth[(Schema: Auth)]
    User -.-> DB_User[(Schema: User)]
    Academic -.-> DB_Academic[(Schema: Academic)]
    LMS -.-> DB_LMS[(Schema: LMS)]
    Grade -.-> DB_Grade[(Schema: Grade)]
    Attendance -.-> DB_Attendance[(Schema: Attendance)]
    Notification -.-> DB_Notif[(Schema: Notification)]
    Admin -.-> DB_Admin[(Schema: Admin)]
```

---

## 2. Thiết kế Modules & Phân vùng Nghiệp vụ (Bounded Contexts)

Mỗi mô-đun nghiệp vụ tại Backend được coi là một **Bounded Context** tự quản lý hoàn toàn logic của chính nó, tương tác qua Java Interfaces nội bộ (Internal APIs) hoặc Application Events (In-memory Event Bus) cho các luồng bất đồng bộ.

| Module | Bounded Context / Trách nhiệm cốt lõi |
| :--- | :--- |
| **Auth** | Quản lý đăng nhập, cấp phát JWT, mã hóa mật khẩu, và quản lý các quyền (RBAC - Role-Based Access Control). |
| **User** | Quản lý thông tin hồ sơ (Profiles), nhân sự nhà trường (Staff, Teacher) và học sinh (Student). Không chứa logic môn học. |
| **Academic** | Định nghĩa cấu trúc học thuật: Năm học, Học kỳ, Danh mục Môn học, Quản lý Lớp học và Thời khóa biểu giảng dạy. |
| **LMS** | (Learning Management System). Quản lý Kho tài liệu học tập, Bài tập (Assignments), và Nhận bài nộp của học sinh (Submissions). |
| **Grade** | Xử lý logic chấm điểm, tính toán hệ số, tổng kết điểm trung bình (GPA), xuất Bảng điểm (Transcripts). |
| **Attendance**| Theo dõi chuyên cần, đánh dấu điểm danh hàng ngày/từng tiết học. |
| **Notification**| Trung tâm gửi thông báo: In-app notifications, Emails, Deadline Reminders. |
| **Admin** | Nơi chứa các cấu hình hệ thống (Configs) và xử lý dữ liệu báo cáo tổng hợp (Dashboards/Reports). |

---

## 3. Nguyên Tắc Thiết Kế Dữ Liệu (Data Isolation & Soft Links)

Để đảm bảo tính độc lập của từng Module (Decoupling) chuẩn bị cho việc tách Microservices sau này nếu cần:
- **Không Foreign Key chéo (No Cross-Schema Foreign Keys):** Database của Module A không được phép tạo khóa ngoại (FK) ràng buộc vật lý trỏ sang Schema của Module B.
- **Sử dụng Soft Links:** Các thực thể liên kết với nhau bằng cách lưu trữ UUID. 
  - *Ví dụ:* Bảng `submissions` trong **LMS Module** sẽ lưu `student_id` (UUID). LMS Module không cần biết cấu trúc bảng của user, khi cần hiển thị tên học sinh, LMS Module sẽ gọi interface sang **User Module** để lấy dữ liệu.
- **In-memory Event Driven:** Sử dụng Spring ApplicationEventPublisher để đồng bộ dữ liệu giữa các module khi cần thiết (thay vì Kafka/RabbitMQ để giữ tính Monolith gọn nhẹ).

---

## 4. Bảo Mật và Xác Thực (Security & Authentication)

Hệ thống áp dụng kiến trúc **Stateless Authentication**:
1. Client gửi request đăng nhập tới API Gateway.
2. Gateway định tuyến tới **Auth Service**.
3. Auth Service xác thực và trả về JWT Token (chứa `user_id` và `role`).
4. Trong các Request tiếp theo, Gateway sẽ validate JWT Token, nếu hợp lệ sẽ forward request cùng các headers chứa thông tin user xuống cho các Microservices bên dưới. Các Microservices không cần tự parse JWT nữa.
