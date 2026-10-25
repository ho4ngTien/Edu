# Phase 1.8 — Thành phần 01: Composition Root Bootstrap

Tài liệu này đặc tả quy chuẩn xây dựng điểm khởi chạy ứng dụng duy nhất (**Composition Root**) tại module `boot` của E-Edu.

---

## 1. Nguyên Tắc Bất Biến Của Composition Root

```
┌────────────────────────────────────────────────────────┐
│                      boot Module                       │
│  (Chứa EduApplication.java, Spring Config, Web Wiring)  │
└───────────────────────────┬────────────────────────────┘
                            │ (Chỉ phụ thuộc 1 chiều ↓)
       ┌────────────────────┼────────────────────┐
       ▼                    ▼                    ▼
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│   identity   │     │ organization │     │    people    │ ... (Các module nghiệp vụ)
└──────────────┘     └──────────────┘     └──────────────┘
```

1. **Phụ thuộc một chiều duy nhất**: Module `boot` phụ thuộc vào tất cả các module nghiệp vụ; các module nghiệp vụ **tuyệt đối không bao giờ được phụ thuộc ngược lại vào `boot`**.
2. **Không chứa logic nghiệp vụ**: `boot` chỉ chịu trách nhiệm cấu hình bean, wiring các interface adapter, cấu hình bảo mật và xử lý ngoại lệ HTTP toàn cục.
3. **Kiểm soát Component Scanning**:
   - Khởi chạy tại `com.edu.boot.EduApplication`.
   - Sử dụng cấu hình scan rõ ràng hoặc nạp qua các cấu hình `@Configuration` tự động của module để tránh việc vô tình nạp các bean thử nghiệm hoặc quét không kiểm soát.

---

## 2. Mã Khởi Động Chuẩn: `EduApplication.java`

```java
package com.edu.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.edu")
@EntityScan(basePackages = "com.edu")
@EnableJpaRepositories(basePackages = "com.edu")
public class EduApplication {

    public static void main(String[] args) {
        SpringApplication.run(EduApplication.class, args);
    }
}
```

---

## 3. Cấu Hình Global Infrastructure Beans Trong `boot`

Module `boot` cung cấp các bean hạ tầng dùng chung mà các module nghiệp vụ cần:
- `Clock`: Bean `java.time.Clock.systemUTC()` hỗ trợ inject thời gian chuẩn UTC và dễ dàng mock thời gian trong unit test.
- `AuditorAware`: Tích hợp với `SecurityContext` để tự động điền `createdBy` và `updatedBy` trong các JPA Entity kế thừa `AuditableEntity`.
- `GlobalExceptionHandler`: Bắt mọi ngoại lệ và format về `ApiErrorResponse`.
