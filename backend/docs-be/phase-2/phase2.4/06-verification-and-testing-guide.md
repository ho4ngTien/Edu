# Phase 2.4 — Thành phần 06: Verification & Testing Guide

Tài liệu này hướng dẫn cách chạy kiểm thử, xác thực kết quả của Phase 2.4 và tích hợp vào quy trình CI/CD.

---

## 1. Lệnh Thực Thi Toàn Bộ Kiểm Thử

Chạy từ thư mục gốc `backend/`:

```bash
mvn clean verify
```

### Quá trình thực thi bao gồm:
1. **Unit tests trong `shared-kernel`**: Kiểm tra tính toàn vẹn và validation của `UserId`, `OrganizationId`.
2. **WebMvc slice tests trong `boot`**: Kiểm tra `GlobalExceptionHandlerTest` bắt chính xác các lỗi validation, HTTP message not readable, business exception.
3. **ArchUnit tests trong `tests/architecture-tests`**: Kiểm tra toàn bộ 9 quy tắc kiến trúc phân tầng, ranh giới package và cấm cyclic dependencies.

---

## 2. Tiêu Chí Đánh Giá Kết Quả Build

```text
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Summary for Parent 0.1.0-SNAPSHOT:
[INFO] 
[INFO] Parent ............................................. SUCCESS [  0.150 s]
[INFO] shared ............................................. SUCCESS [  0.005 s]
[INFO] shared-kernel ...................................... SUCCESS [  1.210 s]
[INFO] modules ............................................ SUCCESS [  0.005 s]
[INFO] identity ........................................... SUCCESS [  0.450 s]
[INFO] organization ....................................... SUCCESS [  0.420 s]
...
[INFO] tests .............................................. SUCCESS [  0.005 s]
[INFO] architecture-tests ................................. SUCCESS [  3.120 s]
[INFO] boot ............................................... SUCCESS [  2.850 s]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

Nếu bất kỳ quy tắc nào của ArchUnit bị vi phạm (ví dụ một class trong `domain` import class của `infrastructure`), lệnh build sẽ ngay lập tức FAILED với mô tả chi tiết dòng mã vi phạm.
