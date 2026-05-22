# Tài liệu Đặc tả REST API - EduIDE

Tài liệu này cung cấp chi tiết các API Endpoint nghiệp vụ dùng để kết nối giữa ứng dụng Frontend (React Client) và Backend (Spring Boot Server) trong hệ thống **EduIDE**.

---

## 1. Cấu hình Giao tiếp API Chung (General Specifications)

* **Base URI:** `/api` (Mặc định máy chủ chạy tại cổng `8080`, cung cấp các endpoint qua tiền tố `/api/...`).
* **CORS Policy:** Tất cả các endpoint đều hỗ trợ CORS toàn cầu (`@CrossOrigin(origins = "*")`) giúp Client kết nối dễ dàng trong môi trường phát triển cục bộ.
* **Định dạng dữ liệu:** Mọi dữ liệu yêu cầu (Request Body) và phản hồi (Response Body) đều tuân thủ định dạng chuẩn **JSON (Application/JSON)**.

---

## 2. Danh mục API Endpoints Chi tiết

### A. Module Workspace (Quản lý Không gian làm việc - `/api/workspace`)

Nhóm API hỗ trợ duyệt, tạo, sửa, xóa và khôi phục không gian làm việc cục bộ của học sinh.

#### 1. Lấy danh sách tệp tin trong Workspace
* **Endpoint:** `GET /api/workspace`
* **Mô tả:** Lấy danh sách toàn bộ các tệp tin và thư mục hiện có trong không gian làm việc của học sinh một cách đệ quy.
* **Response (HTTP 200 OK):**
  ```json
  [
    {
      "path": "Main.java",
      "name": "Main.java",
      "directory": false,
      "content": "public class Main {\n    public static void main(String[] args) {\n..."
    },
    {
      "path": "style.css",
      "name": "style.css",
      "directory": false,
      "content": "body {\n    font-family: 'Outfit', sans-serif;\n..."
    }
  ]
  ```

#### 2. Lưu nội dung tệp tin
* **Endpoint:** `POST /api/workspace/save`
* **Mô tả:** Lưu hoặc cập nhật nội dung văn bản của một tệp tin cụ thể theo đường dẫn.
* **Request Body:**
  ```json
  {
    "path": "index.html",
    "content": "<!DOCTYPE html>\n<html>\n..."
  }
  ```
* **Response (HTTP 200 OK):**
  ```json
  {
    "message": "File saved successfully"
  }
  ```

#### 3. Tạo mới tệp tin hoặc thư mục
* **Endpoint:** `POST /api/workspace/create`
* **Mô tả:** Tạo mới một tệp tin trống hoặc một thư mục mới tại đường dẫn tương đối được cung cấp.
* **Request Body:**
  ```json
  {
    "path": "src/utils.js",
    "isDirectory": false
  }
  ```
* **Response (HTTP 200 OK):**
  ```json
  {
    "message": "Created successfully"
  }
  ```

#### 4. Xóa tệp tin hoặc thư mục
* **Endpoint:** `DELETE /api/workspace`
* **Mô tả:** Xóa vĩnh viễn tệp tin hoặc thư mục (bao gồm tất cả nội dung bên trong).
* **Query Parameter:** `?path=tên_đường_dẫn_file` (Ví dụ: `DELETE /api/workspace?path=src/utils.js`)
* **Response (HTTP 200 OK):**
  ```json
  {
    "message": "Deleted successfully"
  }
  ```

#### 5. Khôi phục Workspace về mặc định
* **Endpoint:** `POST /api/workspace/reset`
* **Mô tả:** Xóa sạch toàn bộ tệp tin trong workspace hiện tại và tái tạo lại tập hợp các tệp tin bài học mặc định (`Main.java`, `index.html`, `style.css`, `script.js`).
* **Response (HTTP 200 OK):** Trả về danh sách cấu trúc tệp mặc định mới tạo (tương tự định dạng phản hồi `GET /api/workspace`).

---

### B. Module Execution (Biên dịch & Thực thi - `/api/execute`)

#### 1. Biên dịch và thực thi mã nguồn
* **Endpoint:** `POST /api/execute`
* **Mô tả:** Biên dịch mã nguồn Java động trên máy chủ và trả về kết quả thực thi. Đối với các ngôn ngữ Web (như JavaScript), backend sẽ trả về nhãn thông báo và client sẽ tự đánh giá cục bộ qua trình duyệt.
* **Request Body:**
  ```json
  {
    "language": "java",
    "code": "public class Main {\n    public static void main(String[] args) {\n        System.out.println(\"Hello World\");\n    }\n}",
    "path": "Main.java"
  }
  ```
* **Response (HTTP 200 OK - Biên dịch thành công):**
  ```json
  {
    "stdout": "Hello World\n",
    "stderr": "",
    "exitCode": 0,
    "compileError": null,
    "executionTime": 142
  }
  ```
* **Response (HTTP 200 OK - Lỗi biên dịch cú pháp):**
  ```json
  {
    "stdout": "",
    "stderr": "",
    "exitCode": 1,
    "compileError": "/temp_compile/uuid/Main.java:3: error: ';' expected\n...",
    "executionTime": 0
  }
  ```

---

### C. Module Curriculum (Học trình & Bài học - `/api/curriculum`)

#### 1. Lấy danh sách toàn bộ bài học
* **Endpoint:** `GET /api/curriculum/lessons`
* **Mô tả:** Lấy lộ trình các bài học được cấu hình sẵn trong hệ thống.
* **Response (HTTP 200 OK):**
  ```json
  [
    {
      "id": "html-basics",
      "title": "Cấp độ 1: Cấu trúc Web (HTML)",
      "subtitle": "Tạo cấu trúc thẻ giao diện cho hộp hồ sơ người dùng",
      "instructions": "<p>Chào mừng bạn đến với bài học đầu tiên!...</p>",
      "hints": [
        "Bạn có thể viết: <div class=\"card\">...",
        "Đảm bảo viết đúng chính tả class và id!"
      ],
      "objectives": [
        {
          "id": "html_heading",
          "description": "Tạo thẻ tiêu đề chính <h1> để chào mừng người dùng",
          "completed": false
        }
      ],
      "targetFile": "index.html"
    }
  ]
  ```

#### 2. Lấy chi tiết bài học theo ID
* **Endpoint:** `GET /api/curriculum/lessons/{id}`
* **Mô tả:** Lấy thông tin chi tiết của một bài học học trình cụ thể.
* **Response (HTTP 200 OK / 404 Not Found):** Trả về đối tượng bài học duy nhất khớp với ID yêu cầu.

#### 3. Nộp bài & Tự động Xác thực kết quả
* **Endpoint:** `POST /api/curriculum/verify/{lessonId}`
* **Mô tả:** Gửi danh sách toàn bộ tệp tin trong workspace hiện tại của học sinh lên máy chủ để hệ thống tự động kiểm tra xem các tiêu chí (Objectives) đã được hoàn thành hay chưa.
* **Request Body:** Gửi danh sách các tệp tin trong Workspace (định dạng tương tự Body nhận được từ `GET /api/workspace`).
* **Response (HTTP 200 OK):**
  ```json
  {
    "objectives": [
      {
        "id": "html_heading",
        "description": "Tạo thẻ tiêu đề chính <h1> để chào mừng người dùng",
        "completed": true
      },
      {
        "id": "html_card",
        "description": "Tạo một thẻ div bọc ngoài có class=\"card\" để hiển thị hộp giao diện",
        "completed": true
      },
      {
        "id": "html_button",
        "description": "Tạo một nút bấm với id=\"action-btn\"",
        "completed": false
      }
    ],
    "success": false,
    "message": "Hãy tiếp tục thử nhé! Một số mục tiêu kiểm tra vẫn chưa hoàn thành."
  }
  ```
