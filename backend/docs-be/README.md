# E-Edu Backend Documentation

Tài liệu kỹ thuật toàn diện cho backend E-Edu — hệ thống quản lý giáo dục xây dựng trên kiến trúc **Modular Monolith Multi-Schema** với Java 21, Spring Boot 3.4 và PostgreSQL 15.

---

## Lộ trình Kỹ thuật

| Phase | Tên | Trạng thái | Mô tả |
| :---: | :--- | :---: | :--- |
| **1** | [Architecture & Requirements](phase-1/README.md) | ✅ Hoàn thành | Domain model, module boundaries, naming conventions |
| **2** | [Project Setup & Core Domain Foundation](phase-2/README.md) | ✅ Hoàn thành | Maven multi-module, Spring Boot profiles, Docker, Shared Kernel |
| **3** | [Foundation & Observability](phase-3/README.md) | 🔄 Hoàn thành một phần | Exception handling, validation, Flyway, logging, Actuator |
| **4** | [Identity, Access & Multi-Tenancy](phase-4/README.md) | 🔄 Hoàn thành một phần | Users, Organizations, Security, JWT, RBAC |
| **5** | [Education Core](phase-5/README.md) | 📋 Kế hoạch | Students, Teachers, Academic, Curriculum, Enrollment |
| **6** | [Academic Operations](phase-6/README.md) | 📋 Kế hoạch | Teaching, Attendance, Assessments, Rule Engine, Certification |
| **7** | [Quality & Delivery](phase-7/README.md) | 🔄 Hoàn thành một phần | Unit Tests, ArchUnit, Integration Tests, OpenAPI, CI/CD |

---

## Công nghệ Sử dụng

| Lớp | Công nghệ |
| :--- | :--- |
| **Language / Runtime** | Java 21 LTS |
| **Framework** | Spring Boot 3.4.3, Spring Security 6 |
| **Build Tool** | Maven Multi-Module (Reactor 19 modules) |
| **Database** | PostgreSQL 15 (Multi-Schema) |
| **Schema Management** | Flyway (V1–V5 triển khai, V6+ kế hoạch) |
| **ORM** | Spring Data JPA / Hibernate |
| **Security** | JWT (HMAC-SHA256), Opaque Refresh Token |
| **Testing** | JUnit 5, Mockito, ArchUnit 1.4.1, Testcontainers |
| **Infrastructure** | Docker Compose, GitHub Actions (kế hoạch) |

---

## Tài liệu Tham khảo Nhanh

- **[Backend Roadmap](backend-roadmap.md)** — Lộ trình đầy đủ với liên kết tài liệu từng phase
- **[MVP Scope & User Roles](MVP-SCOPE-AND-USER-ROLES.md)** — Phạm vi MVP và phân loại vai trò người dùng

---

> Xem lộ trình chi tiết tại [backend-roadmap.md](backend-roadmap.md).
