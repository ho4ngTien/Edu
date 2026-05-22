# Cẩm nang Phát triển & Quy chuẩn Code - EduIDE Team Playbook

Tài liệu này định nghĩa các nguyên tắc thiết kế phần mềm, quy chuẩn mã nguồn (Coding Conventions) và quy trình làm việc chung dành cho tất cả thành viên phát triển dự án **EduIDE**.

---

## 1. Triết lý Thiết kế Kiến trúc (Architectural Philosophy)

Dự án **EduIDE** tuân thủ tuyệt đối triết lý phân tách ranh giới và bảo vệ không gian bộ nhớ:

### A. Kỷ luật ranh giới mô-đun tại Backend (Modular Monolith)
* **Không import chéo tự do:** Mã nguồn thuộc một mô-đun nghiệp vụ (ví dụ: `execution`) không được phép truy cập trực tiếp vào các hàm nội bộ (`internal/service` hoặc cấu trúc riêng) của mô-đun khác (ví dụ: `curriculum`).
* **Truy xuất qua Public Contract:** Các mô-đun chỉ được phép trao đổi dữ liệu thông qua các cổng giao tiếp API chung được định nghĩa công khai, hoặc dùng chung các thực thể chia sẻ được cấp phép.

### B. Tổ chức thư mục Feature-based tại Frontend
* Tất cả các mã nguồn của Frontend đều được gom nhóm tập trung theo tính năng nghiệp vụ nằm trong thư mục `/frontend/src/features/` thay vì gom theo cấu trúc kỹ thuật (như folder chứa toàn bộ component, folder chứa toàn bộ hooks).
* **5 Nhóm Tính năng Cốt lõi:**
  1. `workspace`: Quản lý hiển thị sơ đồ cây thư mục và danh sách file học tập.
  2. `editor`: Quản lý bộ soạn thảo viết mã nguồn trực tuyến.
  3. `curriculum`: Quản lý bảng hướng dẫn, nhiệm vụ học trình, gợi ý giải đề.
  4. `terminal`: Màn hình hiển thị kết quả biên dịch và lỗi hệ thống.
  5. `preview`: Khung trình diễn trực quan kết quả biên dịch web (HTML/CSS/JS).

---

## 2. Quy chuẩn Viết Code Backend (Java 21 Standards)

### A. Sử dụng tính năng mới của Java 21
Khuyến khích tối ưu mã nguồn bằng cách sử dụng các cú pháp hiện đại của Java 21:
* Sử dụng từ khóa khai báo nhanh `var` cho các biến cục bộ trong phương thức khi kiểu dữ liệu đã rõ ràng.
* Ưu tiên sử dụng đối tượng lưu trữ dữ liệu bất biến **`record`** thay cho các class chứa dữ liệu (DTO, Payloads) để giảm bớt boilerplate code (Getter, Setter, Equals, HashCode).
* Sử dụng Switch Expression trả về giá trị trực tiếp để thay thế các khối if-else cồng kềnh.

### B. Tiêu chuẩn Controller Layer
* Tất cả các REST Controller phải khai báo đầy đủ cấu hình `@CrossOrigin(origins = "*")` để tạo điều kiện thuận lợi cho Client gọi API chéo cổng trong quá trình phát triển cục bộ.
* Mọi phương thức trong Controller phải sử dụng các kiểu trả về chuẩn hóa của Spring: `ResponseEntity<?>` để kiểm soát tốt HTTP Status trả về.

### C. Quy tắc Vàng về Vận hành & Bảo mật Đĩa cứng
1. **Directory Traversal Guard:** Mọi tác vụ đọc/ghi đĩa cứng từ không gian học sinh bắt buộc phải đi qua cơ chế giải quyết đường dẫn an toàn `resolveSafe()` để tránh bị hack đĩa cứng.
2. **Cơ chế try-finally dọn dẹp đĩa:** Khi khởi chạy tiến trình biên dịch mã Java tạm thời, bắt buộc phải viết hàm dọn dẹp đĩa cứng `cleanup(tempDir.toFile())` trong khối `finally` để bảo đảm thư mục tạm luôn được xóa sạch dù tiến trình có bị lỗi hoặc ngắt đột ngột.
3. **Giới hạn thời gian chạy (Execution Timeout):** Luôn khống chế thời gian đợi của các tiến trình biên dịch con là **5 giây** để chống treo tài nguyên máy chủ.

---

## 3. Quy chuẩn Thiết kế & Code Frontend (Vite + React)

### A. Thẩm mỹ & Giao diện Cao cấp (Aesthetics & Theme)
* Sử dụng dải màu tối nhất quán làm chủ đạo (Slate/Indigo) như nền tối `#0f172a` và các màu sắc Tailored HSL có tính tương phản dịu mắt.
* Tránh sử dụng các màu cơ bản thuần túy (như màu đỏ tươi, màu xanh lá cây chói), thay bằng các màu được tinh chỉnh hài hòa (như màu lục bảo nhạt `#10b981`, màu chàm `#6366f1`).
* Khuyến khích ứng dụng thiết kế kính mờ hiện đại (**Glassmorphism**) bằng việc sử dụng các thuộc tính viền trong suốt nhẹ `rgba(255, 255, 255, 0.05)` kết hợp hiệu ứng nhòe nền `backdrop-filter: blur(12px)`.

### B. Micro-animations & Trải nghiệm tương tác
* Mọi nút bấm và tương tác rê chuột (hover) phải có hiệu ứng chuyển dịch mềm mại sử dụng lớp chuyển cảnh `transition-all duration-150` kết hợp thay đổi vị trí nhẹ (`translate-y-[-2px]`).
* Khi kiểm tra bài học thành công hoàn toàn (`success=true`), bắt buộc kích hoạt hiệu ứng pháo hoa rực rỡ bằng thư viện **`canvas-confetti`** từ cả 2 góc màn hình trong vòng 2.5 giây để tăng trải nghiệm thành tựu của học sinh.
* Sử dụng biểu tượng động từ bộ thư viện **`lucide-react`** để tạo cảm giác giao diện sống động và trực quan.

---

## 4. Quy trình Quản lý Mã nguồn & Đánh dấu Commit (Git Convention)

### A. Quy tắc đặt tên nhánh (Branching Strategy)
Làm việc dựa trên nhánh phát triển chính `develop`. Các thành viên đặt tên nhánh tính năng theo cấu trúc:
* **Tính năng mới:** `feature/<tên-tính-năng>` (Ví dụ: `feature/java-autocomplete`)
* **Sửa lỗi hệ thống:** `bugfix/<lỗi-cần-sửa>` (Ví dụ: `bugfix/confetti-z-index`)
* **Tối ưu cấu trúc:** `refactor/<tên-khu-vực>` (Ví dụ: `refactor/compiler-service`)

### B. Định dạng Commit Message (Commit Conventions)
Commit message phải viết rõ ràng bằng tiếng Anh (hoặc tiếng Việt có dấu chuẩn chỉ), tuân thủ định dạng gắn thẻ mô-đun bị tác động:
```text
[<MÔ-ĐUN>] <Mô tả ngắn gọn bằng tiếng Anh về thay đổi>
```
*Ví dụ:*
* `[WORKSPACE] Add resolveSafe path verification to prevent Directory Traversal`
* `[COMPILER] Add execution timeout exception handler for infinite loops`
* `[FRONTEND] Fix editor scrollbar style and glassmorphism borders`
* `[CURRICULUM] Complete objectives list rendering logic in React`
