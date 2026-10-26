# Phase 2.1 — Thành phần 02: Root Parent POM Configuration

Tài liệu này đặc tả chi tiết cấu hình tệp `backend/pom.xml` đóng vai trò là Root Parent POM quản lý tập trung toàn bộ phiên bản, plugin và module cho E-Edu.

---

## 1. Thông Số Công Nghệ & Khai Báo Parent

Root POM kế thừa trực tiếp từ `spring-boot-starter-parent` phiên bản `3.4.3`:

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

    <groupId>com.edu</groupId>
    <artifactId>parent</artifactId>
    <version>0.1.0-SNAPSHOT</version>
    <packaging>pom</packaging>

    <name>Parent</name>
    <description>Modular Monolith Parent POM</description>
```

---

## 2. Quản Lý Properties & Dependencies Tập Trung

Toàn bộ phiên bản Java và các thư viện bổ trợ bên ngoài được định nghĩa trong thẻ `<properties>` và `<dependencyManagement>`:

```xml
    <properties>
        <java.version>21</java.version>
        <maven.compiler.release>21</maven.compiler.release>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
        <archunit.version>1.4.1</archunit.version>
    </properties>

    <modules>
        <module>boot</module>
        <module>shared</module>
        <module>modules</module>
        <module>tests</module>
    </modules>

    <dependencyManagement>
        <dependencies>
            <!-- Shared Kernel -->
            <dependency>
                <groupId>com.edu</groupId>
                <artifactId>shared-kernel</artifactId>
                <version>${project.version}</version>
            </dependency>

            <!-- 12 Core Business Modules -->
            <dependency>
                <groupId>com.edu</groupId>
                <artifactId>identity</artifactId>
                <version>${project.version}</version>
            </dependency>
            <dependency>
                <groupId>com.edu</groupId>
                <artifactId>organization</artifactId>
                <version>${project.version}</version>
            </dependency>
            <dependency>
                <groupId>com.edu</groupId>
                <artifactId>configuration</artifactId>
                <version>${project.version}</version>
            </dependency>
            <dependency>
                <groupId>com.edu</groupId>
                <artifactId>people</artifactId>
                <version>${project.version}</version>
            </dependency>
            <dependency>
                <groupId>com.edu</groupId>
                <artifactId>curriculum</artifactId>
                <version>${project.version}</version>
            </dependency>
            <dependency>
                <groupId>com.edu</groupId>
                <artifactId>academic</artifactId>
                <version>${project.version}</version>
            </dependency>
            <dependency>
                <groupId>com.edu</groupId>
                <artifactId>enrollment</artifactId>
                <version>${project.version}</version>
            </dependency>
            <dependency>
                <groupId>com.edu</groupId>
                <artifactId>teaching</artifactId>
                <version>${project.version}</version>
            </dependency>
            <dependency>
                <groupId>com.edu</groupId>
                <artifactId>attendance</artifactId>
                <version>${project.version}</version>
            </dependency>
            <dependency>
                <groupId>com.edu</groupId>
                <artifactId>assessment</artifactId>
                <version>${project.version}</version>
            </dependency>
            <dependency>
                <groupId>com.edu</groupId>
                <artifactId>certification</artifactId>
                <version>${project.version}</version>
            </dependency>
            <dependency>
                <groupId>com.edu</groupId>
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
```

---

## 3. Cấu Hình Compiler Plugin

Đảm bảo `javac` biên dịch với release Java 21 và gắn kèm thông tin tên tham số (parameters) để Spring MVC và JPA reflection hoạt động chính xác:

```xml
    <build>
        <pluginManagement>
            <plugins>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-compiler-plugin</artifactId>
                    <configuration>
                        <release>21</release>
                        <parameters>true</parameters>
                    </configuration>
                </plugin>
            </plugins>
        </pluginManagement>
    </build>
</project>
```
