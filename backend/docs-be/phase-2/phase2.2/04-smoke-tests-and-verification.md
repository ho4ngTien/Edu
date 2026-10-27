# Phase 2.2 — Thành phần 04: Smoke Tests & Spring Context Verification

Tài liệu này đặc tả bộ kiểm thử khởi động (Smoke Tests) nhằm bảo đảm toàn bộ Spring Application Context được nạp thành công mà không có xung đột bean.

---

## 1. Cấu Trúc Smoke Test: `EduApplicationTests.java`

Tệp đặt tại: `boot/src/test/java/com/edu/boot/EduApplicationTests.java`

```java
package com.edu.boot;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class EduApplicationTests {

    @Test
    @DisplayName("Spring Application Context khởi động thành công với profile test")
    void contextLoads() {
        // Nếu toàn bộ bean dependencies, component scan và JPA repositories
        // được wire chính xác mà không gặp vòng lặp hoặc lỗi cấu hình,
        // test này sẽ hoàn tất thành công.
    }
}
```

---

## 2. Quy Trình Chạy Kiểm Thử Khởi Động

Thực thi lệnh Maven từ thư mục gốc `backend/`:

```bash
mvn test -pl boot
```

### Kết quả mong đợi:
```text
[INFO] Running com.edu.boot.EduApplicationTests
2026-10-26T14:20:00.000Z  INFO [edu-backend,,] --- [           main] com.edu.boot.EduApplicationTests : Starting EduApplicationTests using Java 21...
2026-10-26T14:20:01.000Z  INFO [edu-backend,,] --- [           main] com.edu.boot.EduApplicationTests : Started EduApplicationTests in 1.456 seconds
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.82 s -- in com.edu.boot.EduApplicationTests
[INFO] BUILD SUCCESS
```
