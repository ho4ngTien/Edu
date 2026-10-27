# Phase 2.1 — Thành phần 03: Module POMs & Dependencies Management

Tài liệu này đặc tả quy chuẩn khai báo dependencies trong các file `pom.xml` con của E-Edu.

---

## 1. Nguyên Tắc Khai Báo Dependencies

1. **Tuyệt đối không khai báo `<version>` trong các submodule con**:
   - Nếu thư viện đã được khai báo trong `<dependencyManagement>` của Root Parent POM (hoặc kế thừa từ Spring Boot Dependencies BOM), submodule chỉ khai báo `<groupId>` và `<artifactId>`.
2. **Khai báo tối thiểu (Principle of Least Dependency)**:
   - Một module chỉ khai báo những thư viện mà nó thực sự import code.
   - Tránh việc khai báo bừa bãi `spring-boot-starter-web` vào các module chỉ thuần túy xử lý domain hoặc persistence.

---

## 2. Ví Dụ Cấu Hình Submodule POM

### 2.1. Aggregator POM (`modules/pom.xml`)
Chỉ chứa danh sách các module nghiệp vụ con:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.edu</groupId>
        <artifactId>parent</artifactId>
        <version>0.1.0-SNAPSHOT</version>
    </parent>

    <artifactId>modules</artifactId>
    <packaging>pom</packaging>

    <modules>
        <module>identity</module>
        <module>organization</module>
        <module>configuration</module>
        <module>people</module>
        <module>curriculum</module>
        <module>academic</module>
        <module>enrollment</module>
        <module>teaching</module>
        <module>attendance</module>
        <module>assessment</module>
        <module>certification</module>
        <module>rule-engine</module>
    </modules>
</project>
```

### 2.2. Leaf Module POM (`modules/identity/pom.xml`)
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.edu</groupId>
        <artifactId>modules</artifactId>
        <version>0.1.0-SNAPSHOT</version>
    </parent>

    <artifactId>identity</artifactId>
    <packaging>jar</packaging>

    <dependencies>
        <dependency>
            <groupId>com.edu</groupId>
            <artifactId>shared-kernel</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```
