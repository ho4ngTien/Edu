# Phase 2.1 — Thành phần 04: Build & Reactor Verification Guide

Tài liệu này hướng dẫn quy trình biên dịch, kiểm chứng toàn bộ 19 modules và xử lý các lỗi thường gặp trong quá trình build Maven của E-Edu.

---

## 1. Các Lệnh Build Maven Chuẩn

Thực hiện từ thư mục `backend/`:

### 1.1. Kiểm tra Biên dịch Nhanh (Fast Compile)
```bash
mvn clean compile -DskipTests
```
- Xác nhận toàn bộ 19 modules resolve đúng dependencies và mã nguồn Java 21 biên dịch không có lỗi cú pháp.

### 1.2. Biên dịch & Thực thi Toàn bộ Tests (Full Verification)
```bash
mvn clean verify
```
- Chạy toàn bộ Unit tests, WebMvc slice tests và 9 quy tắc kiểm tra kiến trúc của ArchUnit.
- Build đạt kết quả `BUILD SUCCESS` trên toàn bộ Reactor.

### 1.3. Khởi chạy Ứng dụng từ Maven (Local Run)
```bash
mvn -pl boot -am spring-boot:run
```
- Tham số `-pl boot`: Chỉ định module chạy là `boot`.
- Tham số `-am` (also-make): Tự động biên dịch toàn bộ các module phụ thuộc (`shared-kernel`, `identity`, `organization`,...).

---

## 2. Xử Lý Các Sự Cố Thường Gặp (Troubleshooting)

| Triệu chứng lỗi | Nguyên nhân gốc rễ | Hướng xử lý |
| :--- | :--- | :--- |
| `java: invalid source release: 21` | Môi trường JDK cài đặt trên máy là JDK 17 hoặc cũ hơn. | Cài đặt JDK 21 LTS (Amazon Corretto hoặc Eclipse Temurin 21) và đặt `JAVA_HOME`. |
| `Could not resolve dependencies` | Phiên bản snapshot nội bộ chưa được cài vào local repository. | Chạy `mvn clean install -DskipTests` từ thư mục gốc `backend/`. |
| `Non-resolvable parent POM` | Đường dẫn `<relativePath>` trong submodule POM bị sai. | Kiểm tra thẻ `<parent>` trong POM con trỏ đúng về `../pom.xml`. |
| `Package com.edu.xyz does not exist` | Thiếu khai báo dependency của module cần gọi trong `pom.xml`. | Bổ sung dependency tương ứng vào thẻ `<dependencies>` của submodule. |
