# Phase 2.1 — Maven Multi-Module Project Setup

Tài liệu thiết lập bộ khung dự án Maven Multi-Module cho E-Edu, chuẩn hóa môi trường biên dịch **Java 21 LTS**, quản lý phiên bản tập trung qua Root Parent POM và cấu trúc 19 modules độc lập.

---

## 1. Mục Tiêu Kỹ Thuật

1. **Quản lý tập trung (Centralized Dependency Management)**: Toàn bộ phiên bản thư viện (Spring Boot 3.4.3, PostgreSQL, Flyway, ArchUnit, Testcontainers) được quản lý tại Root Parent POM thông qua `dependencyManagement`.
2. **Biên dịch Java 21 LTS nhất quán**: Sử dụng `maven-compiler-plugin` với target/source Java 21 và bật flag `-parameters` để hỗ trợ reflection.
3. **Cấu trúc module hóa rõ ràng**: Thiết lập phân cấp 19 modules (1 Composition Root `boot`, 1 `shared-kernel`, 11 business modules, 1 `architecture-tests`, và 4 POM module cha/aggregator).

---

## 2. Danh Mục Tài Liệu Thành Phần Chi Tiết

| STT | Tài liệu | Mô tả chi tiết |
| :---: | :--- | :--- |
| **01** | [Maven Multi-Module Hierarchy](01-maven-multi-module-hierarchy.md) | Cây thư mục Maven Reactor, phân định 4 nhóm module và nguyên tắc packaging `pom` vs `jar`. |
| **02** | [Root Parent POM Configuration](02-root-parent-pom-configuration.md) | Đặc tả chi tiết file `backend/pom.xml`, cấu hình Java 21, Spring Boot BOM và plugin management. |
| **03** | [Module POMs & Dependencies](03-module-poms-and-dependencies.md) | Khai báo dependencies cho các module con, quy tắc kế thừa version và cấm ghi đè phiên bản tùy tiện. |
| **04** | [Build & Verification Guide](04-build-verification-guide.md) | Hướng dẫn lệnh build Maven Reactor (`mvn clean compile`, `mvn test-compile`), các lỗi thường gặp và cách xử lý. |

---

## 3. Tiêu Chí Nghiệm Thu Hoàn Thành Phase 2.1

- [x] Tạo khung thư mục 19 modules và các file `pom.xml` tương ứng.
- [x] Khai báo Root Parent POM quản lý Java 21 và Spring Boot BOM 3.4.3.
- [x] Thực thi lệnh `mvn clean compile` thành công toàn bộ reactor mà không gặp lỗi dependency resolution.
- [x] Đảm bảo cấu trúc Maven khớp với sơ đồ phụ thuộc kiến trúc đã thống nhất ở Phase 1.

---

> Chuyển tiếp sang: [Phase 2.2 — Spring Boot Bootstrap & Profiles](../phase2.2/README.md)
