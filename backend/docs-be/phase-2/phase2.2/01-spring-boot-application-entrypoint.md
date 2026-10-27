# Phase 2.2 — Thành phần 01: Spring Boot Application Entrypoint

Tài liệu này đặc tả cấu trúc mã nguồn của lớp khởi động chính `EduApplication.java` đặt tại module `boot`.

---

## 1. Mã Nguồn Lớp Khởi Động Chuẩn

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

## 2. Giải Thích Các Khai Báo Annotations

1. **`@SpringBootApplication(scanBasePackages = "com.edu")`**:
   - Cho phép Spring Container quét các Spring Component (`@Service`, `@Component`, `@RestController`) nằm trong package gốc `com.edu` của tất cả các module nghiệp vụ (`com.edu.identity`, `com.edu.organization`,...).
2. **`@EntityScan(basePackages = "com.edu")`**:
   - Chỉ thị cho Hibernate JPA tìm kiếm toàn bộ các lớp JPA Entity (`@Entity`, `@MappedSuperclass`) nằm trong tất cả các module, bao gồm cả `shared-kernel` (`AuditableEntity`).
3. **`@EnableJpaRepositories(basePackages = "com.edu")`**:
   - Tự động phát hiện và khởi tạo các Spring Data JPA Repositories nằm trong các package `infrastructure.persistence.repository` của từng module con.
