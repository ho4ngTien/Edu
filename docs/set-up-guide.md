# Hướng dẫn Thiết lập Môi trường Phát triển - EduIDE

Tài liệu này hướng dẫn chi tiết từng bước cho các lập trình viên mới bắt đầu thiết lập môi trường phát triển cục bộ và vận hành dự án **EduIDE** trên máy tính cá nhân.

---

## 1. Yêu cầu Công cụ (Prerequisites)

Hãy đảm bảo rằng máy tính của bạn đã cài đặt các phần mềm và phiên bản sau trước khi tiến hành thiết lập:

* **Java Development Kit (JDK):** Phiên bản **21** trở lên.
  * *Khuyên dùng:* [Amazon Corretto 21](https://aws.amazon.com/corretto/) hoặc [Eclipse Temurin 21](https://adoptium.net/temurin/releases/?version=21).
  * *Kiểm tra:* Mở Terminal chạy lệnh `java -version` và `javac -version` để chắc chắn phiên bản 21.
* **Node.js:** Phiên bản LTS từ **18.x** trở lên.
  * *Kiểm tra:* `node -v` và `npm -v`.
* **Maven:** (Tùy chọn) Bản 3.8+ dùng để build Java backend. Dự án có thể tự tải thư viện thông qua các công cụ IDE.
* **Git:** Dùng để quản lý mã nguồn.
* **Trình soạn thảo (IDE):** Khuyên dùng **Visual Studio Code (VS Code)** hoặc **IntelliJ IDEA** (cho phần Backend Java).

---

## 2. Thiết lập Môi trường trên VS Code (Cấu hình tích hợp)

Dự án được cấu hình sẵn môi trường phát triển lý tưởng trong thư mục `.ide/`. Khi bạn mở dự án bằng VS Code, hệ thống sẽ tự động nhận diện các thiết lập sau:

### A. Tiện ích đề xuất (Extensions)
Dựa vào tệp [.ide/extensions.json](file:///c:/Users/N07/Documents/GitHub/Edu/.ide/extensions.json), bạn nên cài đặt các Extension đề xuất sau trên VS Code để lập trình tốt nhất:
- `vscjava.vscode-java-pack` (Extension Pack for Java)
- `vmware.vscode-spring-boot-dev-pack` (Spring Boot Extension Pack)
- `dbaeumer.vscode-eslint` (ESLint hỗ trợ kiểm tra code Frontend)

### B. Cấu hình Chạy & Gỡ lỗi (Debugging Configurations)
Tệp [.ide/launch.json](file:///c:/Users/N07/Documents/GitHub/Edu/.ide/launch.json) cung cấp các phiên chạy debug cấu hình sẵn:
1. **EduIDE Backend (Spring Boot):** Chạy lớp khởi động chính `com.edu.ide.IdeApplication` với tham số môi trường `SPRING_PROFILES_ACTIVE=dev`.
2. **EduIDE Frontend (Vite):** Khởi động Vite client trong môi trường node.
3. **EduIDE Full-Stack (Backend + Frontend):** Phiên chạy tổ hợp (Compound Launch) giúp kích hoạt debug đồng thời cả hai phần Frontend và Backend chỉ bằng một nút bấm **F5**.

### C. Nhiệm vụ tích hợp (VS Code Tasks)
Tệp [.ide/tasks.json](file:///c:/Users/N07/Documents/GitHub/Edu/.ide/tasks.json) cung cấp các Task tự động hóa chạy bằng dòng lệnh:
- **Build Backend (Maven):** Chạy lệnh `mvn clean package -DskipTests` tại thư mục `/backend`.
- **Install Frontend Dependencies:** Tự động cài đặt gói phụ thuộc `npm install` cho frontend.
- **Build Frontend (Vite):** Biên dịch mã nguồn React sang dạng tĩnh `npm run build` tại `/frontend`.
- **Run Local Dev Cluster (All Services):** Kích hoạt file chạy nhanh `infrastructure/run-dev.bat`.

---

## 3. Quy trình Thiết lập Từng bước (Step-by-Step Setup)

### Bước 1: Nhân bản mã nguồn từ kho lưu trữ
```bash
git clone <url-kho-luu-tru-du-an>
cd Edu
```

### Bước 2: Khởi động Backend (Spring Boot)
1. Di chuyển vào thư mục backend:
   ```bash
   cd backend
   ```
2. Tải các gói phụ thuộc và chạy ứng dụng Spring Boot:
   ```bash
   mvn spring-boot:run
   ```
3. Trạng thái thành công: Cửa sổ Console hiển thị logo Spring Boot khởi động và ghi nhận lắng nghe cổng **`8080`** (`http://localhost:8080`).

### Bước 3: Khởi động Frontend (React + Vite)
1. Mở một cửa sổ Terminal mới tại thư mục gốc dự án và di chuyển vào frontend:
   ```bash
   cd frontend
   ```
2. Cài đặt các thư viện phụ thuộc:
   ```bash
   npm install
   ```
3. Khởi động môi trường phát triển của Vite:
   ```bash
   npm run dev
   ```
4. Trạng thái thành công: Terminal hiển thị ứng dụng chạy tại cổng **`5173`** (`http://localhost:5173`).

### Bước 4: Kiểm thử Tích hợp
Truy cập trình duyệt theo địa chỉ: [http://localhost:5173](http://localhost:5173). 
Màn hình sẽ hiển thị giao diện EduIDE hoàn chỉnh, danh sách bài học và mã nguồn mẫu được tải trực tiếp từ máy chủ Backend. Bạn có thể soạn thảo, nhấn nút **"Run Code"** để kiểm tra tính năng biên dịch Java.

---

## 4. Giải pháp Thiết lập Nhanh qua Infrastructure Scripts

Nếu bạn không muốn mở nhiều cửa sổ terminal thủ công, hãy sử dụng các kịch bản khởi động tự động có sẵn trong thư mục [infrastructure/](file:///c:/Users/N07/Documents/GitHub/Edu/infrastructure):

### Phương án A: Dành cho lập trình viên (Windows - run-dev.bat)
* Nhấp đúp chuột vào tệp [run-dev.bat](file:///c:/Users/N07/Documents/GitHub/Edu/infrastructure/run-dev.bat) (hoặc chạy trong PowerShell).
* Hệ thống sẽ tự động bật các tab cmd độc lập để chạy Backend và Frontend, sau 5 giây tự mở trình duyệt của bạn lên trang ứng dụng.

### Phương án B: Chạy qua Docker (run-docker.bat)
* Nhấp đúp chuột vào tệp [run-docker.bat](file:///c:/Users/N07/Documents/GitHub/Edu/infrastructure/run-docker.bat) (yêu cầu máy có cài Docker).
* Tiến trình sẽ thực thi `docker-compose` tự build các image và chạy Front-end tại cổng `80` và cổng dự phòng `5173`.

---

## 5. Khắc phục Sự cố Thường gặp (Troubleshooting)

* **Lỗi biên dịch Java Backend (`java.lang.UnsupportedClassVersionError`):**
  * *Nguyên nhân:* Phiên bản JDK cục bộ của bạn nhỏ hơn 21.
  * *Khắc phục:* Cập nhật biến môi trường `JAVA_HOME` trỏ tới JDK 21 trở lên.
* **Lỗi không tìm thấy file khi nộp bài trong IDE:**
  * *Nguyên nhân:* Thư mục gốc chứa tệp chạy biên dịch tạm (`temp_compile/`) hoặc thư mục không gian học sinh (`workspace/`) chưa được cấp quyền ghi.
  * *Khắc phục:* Đảm bảo ứng dụng chạy dưới quyền có thể ghi tệp lên đĩa cứng vật lý của dự án.
