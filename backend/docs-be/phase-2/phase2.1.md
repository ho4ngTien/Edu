# E-Edu — Phase 2.1: Repository & Maven Multi-Module Project

Mục tiêu của phase này là dựng bộ khung Maven Multi-Module cho E-Edu để toàn bộ dự án có thể biên dịch thống nhất bằng **Java 21**, quản lý dependency tập trung và phát triển từng module độc lập về mặt cấu trúc.

Ta bám sát các quyết định kiến trúc đã thống nhất ở Phase 1:
- **Kiến trúc**: Modular Monolith.
- **Build Tool**: Maven Multi-Module.
- **Ngôn ngữ**: Java 21 (LTS).
- **Framework**: Spring Boot 3.4.3.
- **Cơ sở dữ liệu**: PostgreSQL (Multi-Schema).
- **Ranh giới module**: Mỗi module sở hữu mã nguồn và schema dữ liệu riêng biệt.
- **Kiểm thử kiến trúc**: ArchUnit 1.4.1.
- **Phạm vi Phase 2.1**: Thiết lập cấu trúc thư mục, POM cấu hình và lớp Bootstrap tối thiểu; chưa triển khai nghiệp vụ hay database migration thực tế.

---

## 1. Cấu trúc Repository Mục tiêu

```text
e-edu/
├── pom.xml                               # Root Parent POM (quản lý tập trung version, plugin)
├── e-edu-boot/                           # Composition Root: Ứng dụng Spring Boot thực thi
│   └── pom.xml
├── e-edu-shared/                         # Aggregator các thư viện/abstraction dùng chung
│   ├── pom.xml
│   └── shared-kernel/                    # Value objects, base classes dùng chung tối thiểu
│       └── pom.xml
├── e-edu-modules/                        # Aggregator chứa 11 module nghiệp vụ
│   ├── pom.xml
│   ├── identity/pom.xml                  # Xác thực, User, Role, Permission
│   ├── organization/pom.xml              # Đơn vị tổ chức, Campus, Membership
│   ├── configuration/pom.xml             # Cấu hình nghiệp vụ có kiểu
│   ├── people/pom.xml                    # Hồ sơ Person, Student, Teacher
│   ├── curriculum/pom.xml                # Program, Course, Curriculum rules
│   ├── academic/pom.xml                  # AcademicPeriod, ClassSection, Offering
│   ├── enrollment/pom.xml                # Đăng ký khóa học và chương trình
│   ├── teaching/pom.xml                  # Phân công giảng dạy, lịch trình
│   ├── attendance/pom.xml                # Điểm danh và chuyên cần
│   ├── assessment/pom.xml                # Đánh giá, bài thi, bảng điểm
│   ├── certification/pom.xml             # Chứng nhận, điều kiện tốt nghiệp
│   └── rule-engine/pom.xml               # Bộ thẩm định quy tắc theo phiên bản (AD-04)
└── e-edu-tests/                          # Aggregator kiểm thử xuyên suốt
    ├── pom.xml
    └── architecture-tests/               # Kiểm thử kiến trúc tự động với ArchUnit
        └── pom.xml
```

### Phân định Trách nhiệm 3 Loại POM:

| Loại POM | Trách nhiệm chính | Đóng gói (`packaging`) |
| :--- | :--- | :--- |
| **Root Parent POM** | Quản lý phiên bản Java, Spring Boot BOM, plugin, `dependencyManagement` tập trung và khai báo các aggregator module. | `pom` |
| **Aggregator POM** | Nhóm các module con liên quan (`e-edu-shared`, `e-edu-modules`, `e-edu-tests`), hỗ trợ điều phối build Maven. | `pom` |
| **Leaf Module POM** | Khai báo các dependency nội bộ và bên ngoài mà module đó trực tiếp sử dụng. | `jar` |
| **Boot POM** | Đóng gói executable JAR, tích hợp `spring-boot-maven-plugin` và lắp ráp các module nghiệp vụ. | `jar` |

---

## 2. Chuẩn hóa Phiên bản & Quy ước Công nghệ

| Thông số | Giá trị chuẩn | Ghi chú |
| :--- | :--- | :--- |
| `groupId` | `com.eedu` | Định danh nhóm gói chuẩn của toàn bộ hệ thống |
| `version` | `0.1.0-SNAPSHOT` | Phiên bản khởi đầu cho chu kỳ phát triển |
| `java.version` | `21` | Java 21 LTS |
| `spring.boot.version` | `3.4.3` | Phiên bản Spring Boot 3.4 LTS chính thức, tương thích hoàn toàn Java 21 |
| `archunit.version` | `1.4.1` | Thư viện kiểm thử kiến trúc |
| `maven.version` | `3.9.x` trở lên | Build tool yêu cầu |
| `sourceEncoding` | `UTF-8` | Mã hóa tệp tin thống nhất |

> [!IMPORTANT]
> Dự án sử dụng **Spring Boot 3.4.3** (Spring Framework 6.2). Toàn bộ khai báo `javax.*` chuyển sang `jakarta.*` (Jakarta EE 10). Không sử dụng các phiên bản thử nghiệm không tồn tại.

---

## 3. Cấu hình Root Parent POM (`pom.xml`)

Tệp `pom.xml` tại thư mục gốc quản lý toàn bộ phiên bản thư viện và danh sách module:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.4.3</version>
        <relativePath/>
    </parent>

    <groupId>com.eedu</groupId>
    <artifactId>e-edu-parent</artifactId>
    <version>0.1.0-SNAPSHOT</version>
    <packaging>pom</packaging>

    <name>E-Edu Parent</name>
    <description>E-Edu Modular Monolith Parent POM</description>

    <properties>
        <java.version>21</java.version>
        <maven.compiler.release>21</maven.compiler.release>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
        <archunit.version>1.4.1</archunit.version>
    </properties>

    <modules>
        <module>e-edu-boot</module>
        <module>e-edu-shared</module>
        <module>e-edu-modules</module>
        <module>e-edu-tests</module>
    </modules>

    <dependencyManagement>
        <dependencies>
            <!-- Shared Kernel -->
            <dependency>
                <groupId>com.eedu</groupId>
                <artifactId>shared-kernel</artifactId>
                <version>${project.version}</version>
            </dependency>

            <!-- 11 Core Business Modules -->
            <dependency>
                <groupId>com.eedu</groupId>
                <artifactId>identity</artifactId>
                <version>${project.version}</version>
            </dependency>
            <dependency>
                <groupId>com.eedu</groupId>
                <artifactId>organization</artifactId>
                <version>${project.version}</version>
            </dependency>
            <dependency>
                <groupId>com.eedu</groupId>
                <artifactId>configuration</artifactId>
                <version>${project.version}</version>
            </dependency>
            <dependency>
                <groupId>com.eedu</groupId>
                <artifactId>people</artifactId>
                <version>${project.version}</version>
            </dependency>
            <dependency>
                <groupId>com.eedu</groupId>
                <artifactId>curriculum</artifactId>
                <version>${project.version}</version>
            </dependency>
            <dependency>
                <groupId>com.eedu</groupId>
                <artifactId>academic</artifactId>
                <version>${project.version}</version>
            </dependency>
            <dependency>
                <groupId>com.eedu</groupId>
                <artifactId>enrollment</artifactId>
                <version>${project.version}</version>
            </dependency>
            <dependency>
                <groupId>com.eedu</groupId>
                <artifactId>teaching</artifactId>
                <version>${project.version}</version>
            </dependency>
            <dependency>
                <groupId>com.eedu</groupId>
                <artifactId>attendance</artifactId>
                <version>${project.version}</version>
            </dependency>
            <dependency>
                <groupId>com.eedu</groupId>
                <artifactId>assessment</artifactId>
                <version>${project.version}</version>
            </dependency>
            <dependency>
                <groupId>com.eedu</groupId>
                <artifactId>certification</artifactId>
                <version>${project.version}</version>
            </dependency>
            <dependency>
                <groupId>com.eedu</groupId>
                <artifactId>rule-engine</artifactId>
                <version>${project.version}</version>
            </dependency>

            <!-- Architecture Testing -->
            <dependency>
                <groupId>com.tngtech.archunit</groupId>
                <artifactId>archunit-junit5</artifactId>
                <version>${archunit.version}</version>
                <scope>test</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

</project>
```

---

## 4. Cấu hình các Aggregator POM

### 4.1. `e-edu-shared/pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.eedu</groupId>
        <artifactId>e-edu-parent</artifactId>
        <version>0.1.0-SNAPSHOT</version>
        <relativePath>../pom.xml</relativePath>
    </parent>

    <artifactId>e-edu-shared</artifactId>
    <packaging>pom</packaging>

    <modules>
        <module>shared-kernel</module>
    </modules>

</project>
```

### 4.2. `e-edu-modules/pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.eedu</groupId>
        <artifactId>e-edu-parent</artifactId>
        <version>0.1.0-SNAPSHOT</version>
        <relativePath>../pom.xml</relativePath>
    </parent>

    <artifactId>e-edu-modules</artifactId>
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

### 4.3. `e-edu-tests/pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.eedu</groupId>
        <artifactId>e-edu-parent</artifactId>
        <version>0.1.0-SNAPSHOT</version>
        <relativePath>../pom.xml</relativePath>
    </parent>

    <artifactId>e-edu-tests</artifactId>
    <packaging>pom</packaging>

    <modules>
        <module>architecture-tests</module>
    </modules>

</project>
```

---

## 5. Cấu hình POM cho Từng Module Con

Mọi module nghiệp vụ kế thừa trực tiếp từ `e-edu-parent`. Ví dụ cho `e-edu-modules/people/pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.eedu</groupId>
        <artifactId>e-edu-parent</artifactId>
        <version>0.1.0-SNAPSHOT</version>
        <relativePath>../../pom.xml</relativePath>
    </parent>

    <artifactId>people</artifactId>
    <packaging>jar</packaging>

    <name>E-Edu People Module</name>

    <dependencies>
        <dependency>
            <groupId>com.eedu</groupId>
            <artifactId>shared-kernel</artifactId>
        </dependency>
    </dependencies>

</project>
```

Áp dụng mẫu trên cho 11 module còn lại (`identity`, `organization`, `configuration`, `curriculum`, `academic`, `enrollment`, `teaching`, `attendance`, `assessment`, `certification`, `rule-engine`) và `shared-kernel`.

---

## 6. Cấu hình Composition Root (`e-edu-boot/pom.xml`)

Module `e-edu-boot` kết nối các module nghiệp vụ và đóng gói ứng dụng Spring Boot chạy được:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.eedu</groupId>
        <artifactId>e-edu-parent</artifactId>
        <version>0.1.0-SNAPSHOT</version>
        <relativePath>../pom.xml</relativePath>
    </parent>

    <artifactId>e-edu-boot</artifactId>
    <packaging>jar</packaging>

    <name>E-Edu Application Boot</name>

    <dependencies>
        <!-- Web & Web MVC Layer -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Validation -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <!-- Actuator for Health & Metrics -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <!-- Internal Modular Monolith Modules -->
        <dependency>
            <groupId>com.eedu</groupId>
            <artifactId>shared-kernel</artifactId>
        </dependency>
        <dependency>
            <groupId>com.eedu</groupId>
            <artifactId>identity</artifactId>
        </dependency>
        <dependency>
            <groupId>com.eedu</groupId>
            <artifactId>organization</artifactId>
        </dependency>
        <dependency>
            <groupId>com.eedu</groupId>
            <artifactId>configuration</artifactId>
        </dependency>
        <dependency>
            <groupId>com.eedu</groupId>
            <artifactId>people</artifactId>
        </dependency>
        <dependency>
            <groupId>com.eedu</groupId>
            <artifactId>curriculum</artifactId>
        </dependency>
        <dependency>
            <groupId>com.eedu</groupId>
            <artifactId>academic</artifactId>
        </dependency>
        <dependency>
            <groupId>com.eedu</groupId>
            <artifactId>enrollment</artifactId>
        </dependency>
        <dependency>
            <groupId>com.eedu</groupId>
            <artifactId>teaching</artifactId>
        </dependency>
        <dependency>
            <groupId>com.eedu</groupId>
            <artifactId>attendance</artifactId>
        </dependency>
        <dependency>
            <groupId>com.eedu</groupId>
            <artifactId>assessment</artifactId>
        </dependency>
        <dependency>
            <groupId>com.eedu</groupId>
            <artifactId>certification</artifactId>
        </dependency>
        <dependency>
            <groupId>com.eedu</groupId>
            <artifactId>rule-engine</artifactId>
        </dependency>

        <!-- Spring Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>

</project>
```

---

## 7. Cấu hình `architecture-tests/pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.eedu</groupId>
        <artifactId>e-edu-parent</artifactId>
        <version>0.1.0-SNAPSHOT</version>
        <relativePath>../../pom.xml</relativePath>
    </parent>

    <artifactId>architecture-tests</artifactId>
    <packaging>jar</packaging>

    <name>E-Edu Architecture Tests</name>

    <dependencies>
        <dependency>
            <groupId>com.tngtech.archunit</groupId>
            <artifactId>archunit-junit5</artifactId>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

</project>
```

---

## 8. Lớp Khởi động Ứng dụng Tối thiểu

Tạo tệp `e-edu-boot/src/main/java/com/eedu/boot/EEduApplication.java`:

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

---

## 9. Quy trình Xác thực Build Maven

Thực thi các lệnh sau từ thư mục gốc của backend:

### Bước 1: Kiểm tra môi trường máy phát triển
```bash
java -version
mvn -version
```
Yêu cầu: Java runtime là phiên bản 21 và Maven từ 3.9 trở lên.

### Bước 2: Kiểm tra cấu trúc POM và Dependency Graph
```bash
mvn validate
```
Kiểm tra tính hợp lệ của cây module và các aggregator POM.

### Bước 3: Biên dịch và chạy toàn bộ kiểm thử
```bash
mvn clean verify
```
Chạy từ thư mục gốc theo Maven Reactor, tự động sắp xếp thứ tự biên dịch theo đồ thị phụ thuộc (DAG).

### Bước 4: Kiểm tra cây phụ thuộc module
```bash
mvn dependency:tree
```

### Bước 5: Đóng gói kiểm tra Executable JAR
```bash
mvn -pl e-edu-boot -am package
```
Tham số `-am` (also-make) tự động build các submodule mà `e-edu-boot` phụ thuộc.

---

## 10. Checklist Nghiệm thu Phase 2.1

**Tiến độ Nghiệm thu Phase 2.1 (8/8)**

- [x] Root Parent POM có packaging `pom` và khai báo đầy đủ các aggregator module
- [x] Tất cả module có POM hợp lệ và kế thừa đúng relativePath từ parent POM
- [x] Java compiler release được khóa chặt chẽ ở phiên bản `21`
- [x] Phiên bản dependency được quản lý tập trung qua `dependencyManagement` và Spring Boot 3.4.3 BOM
- [x] `e-edu-boot` khai báo đầy đủ các module nội bộ và `spring-boot-maven-plugin`
- [x] `architecture-tests` khai báo ArchUnit JUnit 5 và JUnit Jupiter
- [x] Lệnh `mvn validate` chạy thành công không có cảnh báo rò rỉ POM
- [x] Lệnh `mvn clean verify` biên dịch toàn bộ các submodule và tạo JAR thành công
