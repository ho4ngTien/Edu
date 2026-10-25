# Phase 1.4 — Thành phần 01: Maven Reactor & Packaging Hierarchy

Tài liệu này đặc tả cấu trúc cây dự án Maven Multi-Module, phân cấp các file `pom.xml` và chiến lược đóng gói (Packaging) trong E-Edu.

---

## 1. Cấu Trúc Cây Thư Mục Toàn Dự Án

```text
backend/
├── pom.xml                               # [Root Parent POM] - Quản lý versions, BOMs, plugins
├── boot/                                 # [Boot POM] - Composition Root, Spring Boot Executable JAR
│   ├── pom.xml
│   └── src/main/java/com/edu/boot/
│       ├── EduApplication.java
│       ├── config/
│       └── web/
├── shared/                               # [Aggregator POM]
│   ├── pom.xml
│   └── shared-kernel/                    # [Leaf POM] - Base types, IDs, DomainEvent
│       └── pom.xml
├── modules/                              # [Aggregator POM]
│   ├── pom.xml
│   ├── identity/                         # [Leaf POM] - Schema identity
│   ├── organization/                     # [Leaf POM] - Schema organization
│   ├── configuration/                    # [Leaf POM] - Schema configuration
│   ├── people/                           # [Leaf POM] - Schema people
│   ├── curriculum/                       # [Leaf POM] - Schema curriculum
│   ├── academic/                         # [Leaf POM] - Schema academic
│   ├── enrollment/                       # [Leaf POM] - Schema enrollment
│   ├── teaching/                         # [Leaf POM] - Schema teaching
│   ├── attendance/                       # [Leaf POM] - Schema attendance
│   ├── assessment/                       # [Leaf POM] - Schema assessment
│   ├── certification/                    # [Leaf POM] - Schema certification
│   └── rule-engine/                      # [Leaf POM] - Schema rule_engine
└── tests/                                # [Aggregator POM]
    ├── pom.xml
    └── architecture-tests/               # [Leaf POM] - ArchUnit architecture verification
        └── pom.xml
```

---

## 2. Vai Trò Của Bốn Loại POM

| Loại POM | Vị trí | Packaging | Vai trò kỹ thuật |
| :--- | :--- | :---: | :--- |
| **Root Parent POM** | `backend/pom.xml` | `pom` | 1. Quản lý phiên bản Java (21 LTS).<br/>2. Khai báo Spring Boot Dependencies BOM trong `<dependencyManagement>`.<br/>3. Chuẩn hóa versions của PostgreSQL driver, Flyway, ArchUnit, Testcontainers.<br/>4. Cấu hình các compiler & surefire plugins. |
| **Aggregator POM** | `shared/`, `modules/`, `tests/` | `pom` | Đóng vai trò gom nhóm các module con để Maven Reactor có thể build song song hoặc theo thứ tự phân cấp khi chạy lệnh từ thư mục gốc. |
| **Leaf Module POM** | Từng module con (ví dụ: `modules/identity/pom.xml`) | `jar` | Khai báo các thư viện cụ thể mà module đó trực tiếp sử dụng (chỉ kế thừa version từ Root Parent, không tự chỉ định version tùy tiện). |
| **Boot Executable POM** | `boot/pom.xml` | `jar` | Tích hợp plugin `spring-boot-maven-plugin` để đóng gói thành **Fat JAR (Executable JAR)** duy nhất phục vụ triển khai production hoặc Docker image. |
