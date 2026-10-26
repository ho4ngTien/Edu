# Phase 2.2 — Thành phần 03: Configuration Properties Validation & Fail-Fast

Tài liệu này đặc tả cơ chế kiểm tra tính hợp lệ của các thông số cấu hình khi ứng dụng khởi chạy nhằm phát hiện lỗi sớm và ngăn ngừa việc khởi động trong trạng thái thiếu an toàn.

---

## 1. Nguyên Tắc Fail-Fast

> **Triết lý Fail-Fast:**  
> Nếu một thông số cấu hình bắt buộc (như `DB_PASSWORD` hoặc `JWT_SECRET_BASE64`) bị thiếu hoặc sai định dạng khi chạy trên môi trường `prod`, ứng dụng phải lập tức dừng tiến trình khởi động (`crash`) và in thông báo lỗi rõ ràng thay vì tiếp tục chạy rồi gây lỗi lúc người dùng gửi request.

---

## 2. Kiểm Tra Cấu Hình Bằng `@ConfigurationProperties`

Sử dụng bean bọc cấu hình có tích hợp Jakarta Bean Validation (`@Validated`):

```java
package com.edu.boot.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "edu.security.jwt")
public record JwtProperties(
    @NotBlank(message = "JWT issuer không được để trống")
    String issuer,

    @NotBlank(message = "JWT secret base64 bắt buộc phải được cấu hình")
    String secretBase64,

    @Positive(message = "Access token TTL phải là số nguyên dương (giây)")
    long accessTokenTtlSeconds,

    @Positive(message = "Refresh token TTL phải là số nguyên dương (giây)")
    long refreshTokenTtlSeconds
) {}
```

Nếu một trong các thuộc tính trên không thỏa mãn validation (ví dụ: `secretBase64` rỗng), Spring Boot sẽ ném ngoại lệ `ConfigurationPropertiesBindException` trong giai đoạn bootstrap và hiển thị thông báo lỗi chi tiết trên console.
