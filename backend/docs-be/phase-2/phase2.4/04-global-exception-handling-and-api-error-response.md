# Phase 2.4 — Thành phần 04: Global Exception Handling & ApiErrorResponse

Tài liệu này đặc tả cơ chế bắt lỗi toàn cục bằng `@RestControllerAdvice` và cấu trúc phản hồi lỗi chuẩn hóa `ApiErrorResponse` trong E-Edu.

---

## 1. Cấu Trúc Record `ApiErrorResponse.java`

Tệp đặt tại: `boot/src/main/java/com/edu/boot/web/ApiErrorResponse.java`

```java
package com.edu.boot.web;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(
    Instant timestamp,
    int status,
    String code,
    String message,
    String path,
    List<FieldErrorItem> fieldErrors
) {
    public record FieldErrorItem(String field, String message) {}

    public static ApiErrorResponse of(int status, String code, String message, String path) {
        return new ApiErrorResponse(Instant.now(), status, code, message, path, List.of());
    }

    public static ApiErrorResponse withFieldErrors(int status, String code, String message, String path, List<FieldErrorItem> fieldErrors) {
        return new ApiErrorResponse(Instant.now(), status, code, message, path, fieldErrors);
    }
}
```

---

## 2. Lớp Xử Lý Ngoại Lệ Toàn Cục: `GlobalExceptionHandler.java`

Tệp đặt tại: `boot/src/main/java/com/edu/boot/web/GlobalExceptionHandler.java`

Bắt các nhóm lỗi chính:
1. **Validation Errors (`MethodArgumentNotValidException`)**:
   - Trả về HTTP 400 và danh sách chi tiết các trường vi phạm trong `fieldErrors`.
2. **Business Errors (`BusinessException`)**:
   - Trả về mã lỗi nghiệp vụ tương ứng (`code`) và HTTP status tương thích.
3. **Resource Not Found (`EntityNotFoundException` / `ResourceNotFoundException`)**:
   - Trả về HTTP 404 và mã `RESOURCE_NOT_FOUND`.
4. **Duplicate Conflicts (`DuplicateResourceException`)**:
   - Trả về HTTP 409 và mã `DUPLICATE_RESOURCE`.
5. **System Unexpected Errors (`Exception`)**:
   - Trả về HTTP 500 và mã `INTERNAL_ERROR`.
   - Log toàn bộ stack trace trên server; tuyệt đối không trả stack trace ra JSON để phòng tránh lộ lọt thông tin nhạy cảm.
