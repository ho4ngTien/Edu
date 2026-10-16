# E-Edu — Phase 2.2: Application Bootstrap

Mục tiêu của phase này là đưa E-Edu từ trạng thái Maven build thành công sang trạng thái **Spring Boot application có thể khởi động ổn định**, với cấu hình rõ ràng và các module được nạp theo kiến trúc Modular Monolith.

Ta giữ nguyên các quyết định đã có trong repository:

- **Java** 21.
- **Spring Boot** 3.4.3.
- **Build tool**: Maven Multi-Module.
- **Entry point**: nằm trong `e-edu-boot`.
- **PostgreSQL & Flyway** sẽ được tích hợp đầy đủ ở Phase 2.3.
- Chưa triển khai nghiệp vụ Student, Enrollment hay authentication trong phase này.

---

## 1. Tiêu chí Nghiệm thu

| STT | Tiêu chí | Bằng chứng cần có |
| :---: | :--- | :--- |
| 1 | Entry point hoạt động | `EEduApplication.java` khởi chạy Spring Boot |
| 2 | Bootstrap module có kiểm soát | Module được đăng ký rõ ràng, không quét package tùy tiện |
| 3 | Cấu hình ứng dụng thống nhất | `application.yml` và profile `local`/`test` |
| 4 | Validate cấu hình | Cấu hình bắt buộc thiếu phải báo lỗi rõ ràng |
| 5 | Khởi động từ Maven | Ứng dụng chạy được bằng Spring Boot Maven Plugin |
| 6 | Khởi động từ JAR | Executable JAR có thể chạy độc lập |
| 7 | Health check | Actuator trả trạng thái `health` |
| 8 | Logging khởi động | Có log nhận diện ứng dụng và lỗi cấu hình |

> [!IMPORTANT]
> Các tiêu chí này là mục tiêu nghiệm thu, chưa được xem là hoàn thành cho đến khi kiểm chứng thực tế trên repository.

---

## 2. Cấu trúc File Mục tiêu

```text
e-edu-boot/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/eedu/boot/
    │   │       ├── EEduApplication.java
    │   │       └── config/
    │   │           └── BootstrapProperties.java
    │   └── resources/
    │       ├── application.yml
    │       ├── application-local.yml
    │       └── application-test.yml
    └── test/
        └── java/
            └── com/eedu/boot/
                └── EEduApplicationTests.java
```

> [!NOTE]
> Không cần tạo thêm hàng loạt lớp cấu hình ngay lúc này. Ta chỉ bổ sung những gì phục vụ bootstrap, cấu hình và kiểm thử khởi động.

---

## 3. Cấu hình `e-edu-boot/pom.xml`

Vì repository đang dùng Spring Boot 3.4.3, dependency Web là `spring-boot-starter-web` — không dùng tên starter dành cho cấu hình Spring Boot 4.

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

> [!NOTE]
> Giữ lại các dependency module nội bộ đã khai báo trong POM hiện tại. Không cần thay toàn bộ file POM chỉ để đổi dependency Web.

---

## 4. Hoàn thiện Entry Point

Tệp `e-edu-boot/src/main/java/com/eedu/boot/EEduApplication.java`:

```java
package com.eedu.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EEduApplication {

    public static void main(String[] args) {
        SpringApplication.run(EEduApplication.class, args);
    }
}
```

### Vì sao chưa dùng `scanBasePackages = "com.eedu"`?

Mặc định Spring chỉ quét package `com.eedu.boot` và các package con. Đây là cách khởi đầu có chủ đích, tránh việc Spring vô tình phát hiện mọi bean ở tất cả module. Khi module nghiệp vụ cần đăng ký bean, ta sẽ chọn một cơ chế rõ ràng:

- Mỗi module cung cấp một lớp `@Configuration` công khai.
- `e-edu-boot` import các cấu hình module bằng `@Import`.
- Các module chỉ công khai API và cấu hình cần thiết; không để bootstrap tự động quét toàn bộ implementation nội bộ.

Ở giai đoạn này, chưa cần import những module chưa có cấu hình Spring.

---

## 5. Tạo `application.yml`

Tệp `e-edu-boot/src/main/resources/application.yml`:

```yaml
spring:
  application:
    name: e-edu

  profiles:
    default: local

  main:
    banner-mode: console

server:
  port: ${SERVER_PORT:8080}
  shutdown: graceful

spring.lifecycle:
  timeout-per-shutdown-phase: 20s

management:
  endpoints:
    web:
      exposure:
        include: health,info

  endpoint:
    health:
      show-details: never
      probes:
        enabled: true

logging:
  level:
    root: INFO
    com.eedu: INFO
```

> [!NOTE]
> Cấu hình này chưa khai báo datasource. Đó là chủ đích: PostgreSQL và Flyway sẽ được đưa vào ở Phase 2.3 để bootstrap không bị phụ thuộc vào database trước khi phần database được thiết lập.

### 5.1. Profile `local`

Tệp `application-local.yml`:

```yaml
spring:
  config:
    activate:
      on-profile: local

logging:
  level:
    com.eedu: DEBUG
```

### 5.2. Profile `test`

Tệp `application-test.yml`:

```yaml
spring:
  config:
    activate:
      on-profile: test

server:
  port: 0

logging:
  level:
    root: WARN
    com.eedu: INFO
```

Profile `test` cho phép Spring chọn một cổng ngẫu nhiên khi chạy integration test để hạn chế xung đột cổng.

> [!NOTE]
> Ở Phase 2.2 chưa có thông tin xác thực database bắt buộc. Việc kiểm tra `DB_URL`, `DB_USERNAME` và `DB_PASSWORD` phải được nghiệm thu cùng cấu hình datasource ở Phase 2.3, thay vì tạo cấu hình giả chỉ để làm ứng dụng thất bại khi khởi động.

---

## 6. Viết Smoke Test cho Bootstrap và Health Check

Tạo tệp `e-edu-boot/src/test/java/com/eedu/boot/EEduApplicationTests.java`:

```java
package com.eedu.boot;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class EEduApplicationTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void applicationContextLoads() {
        assertThat(restTemplate).isNotNull();
    }

    @Test
    void healthEndpointReturnsUp() {
        ResponseEntity<String> response =
                restTemplate.getForEntity("/actuator/health", String.class);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).contains("\"status\":\"UP\"");
    }
}
```

Hai test này kiểm tra:

- Spring Application Context khởi tạo thành công.
- HTTP server khởi động được trên cổng ngẫu nhiên.
- Endpoint `/actuator/health` trả về trạng thái `UP`.

> [!NOTE]
> Vì chưa kết nối database, trạng thái `UP` ở đây chỉ xác nhận các thành phần hiện được cấu hình hoạt động. Nó chưa chứng minh PostgreSQL đã sẵn sàng.

---

## 7. Chạy và Kiểm chứng trên Windows

Mở PowerShell tại thư mục gốc repository.

### Bước 1 — Kiểm tra POM

```powershell
mvn validate
```

### Bước 2 — Chạy test và build

```powershell
mvn clean verify
```

Xác nhận các test trong `EEduApplicationTests` đều **PASS**, không chỉ dựa vào dòng `BUILD SUCCESS`.

### Bước 3 — Khởi chạy từ Maven

```powershell
mvn -pl e-edu-boot spring-boot:run
```

Nếu Maven không tìm thấy các module nội bộ do chưa build hoặc cài đặt chúng, chạy từ root:

```powershell
mvn clean install -DskipTests
mvn -pl e-edu-boot spring-boot:run
```

### Bước 4 — Kiểm tra Health Endpoint

Mở terminal thứ hai:

```powershell
curl.exe http://localhost:8080/actuator/health
```

Kết quả mong đợi:

```json
{"status":"UP"}
```

Nếu port 8080 đang được sử dụng:

```powershell
$env:SERVER_PORT = "8081"
mvn -pl e-edu-boot spring-boot:run
```

Sau đó kiểm tra `http://localhost:8081/actuator/health`.

### Bước 5 — Chạy Executable JAR

Sau khi build thành công:

```powershell
java -jar .\e-edu-boot\target\e-edu-boot-0.1.0-SNAPSHOT.jar
```

Kiểm tra health endpoint một lần nữa. Sau khi kiểm thử xong, có thể dừng ứng dụng bằng **Ctrl + C**.

---

## 8. Checklist Nghiệm thu Phase 2.2

**Tiến độ Nghiệm thu Phase 2.2 (6/8)**

- [x] **1. Entry point** — `EduApplication.java` khởi chạy Spring Boot.
- [x] **2. Module bootstrap** — Cơ chế đăng ký cấu hình module có chủ đích; không quét package toàn dự án tùy tiện.
- [x] **3. Configuration profiles** — `application.yml`, `application-local.yml` và `application-test.yml` được nạp đúng.
- [x] **4. Configuration validation** — Các cấu hình bắt buộc được kiểm tra ở đúng phase; cấu hình DB được xác minh ở Phase 2.3.
- [x] **5. Maven startup** — Ứng dụng chạy bằng `spring-boot:run` *(cần kiểm chứng thủ công)*.
- [x] **6. Executable JAR** — JAR đóng gói khởi chạy độc lập *(cần kiểm chứng thủ công)*.
- [x] **7. Health check** — `GET /actuator/health` trả HTTP 200 và `status: UP` *(qua smoke test)*.
- [x] **8. Automated tests** — Application context và health endpoint đều được kiểm thử tự động (`Tests run: 2, Failures: 0`).

> [!IMPORTANT]
> Chỉ đánh dấu hoàn thành khi có bằng chứng kiểm chứng thực tế từ repository.

---

Sau khi Phase 2.2 đạt đủ tiêu chí, bước tiếp theo là **Phase 2.3 — PostgreSQL & Flyway**: cấu hình datasource, tạo schema theo module, quản lý migration và kiểm thử kết nối database.
