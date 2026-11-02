# Phase 2 — Project Setup & Core Domain Foundation

Tài liệu kỹ thuật thiết lập dự án, hạ tầng cơ sở dữ liệu và Shared Kernel dùng chung cho toàn bộ backend E-Edu.

---

## Danh mục Tài liệu Kỹ thuật

| Phase | Tài liệu | Trạng thái | Mô tả trọng tâm |
| :---: | :--- | :---: | :--- |
| **2.1** | [Maven Multi-Module Project](phase2.1.md) | ✅ Hoàn thành | Thiết lập khung dự án Maven Reactor 19 modules, dependency management và plugin compiler. |
| **2.2** | [Spring Boot Bootstrap & Profiles](phase2.2.md) | ✅ Hoàn thành | Cấu hình Java 21 LTS, Spring Boot 3.4.3, các profile `local`, `test`, `prod` và smoke tests. |
| **2.3** | [Docker Compose & PostgreSQL Multi-Schema](phase2.3.md) | ✅ Hoàn thành | Thiết lập container PostgreSQL 15, phân tách schemas độc lập và cấu hình Flyway migrations. |
| **2.4** | [Shared Kernel & Domain Foundation](phase2.4.md) | ✅ Hoàn thành | Strongly typed IDs (`UserId`, `OrganizationId`), `AuditableEntity`, `DomainEvent`, `ApiErrorResponse`, ArchUnit. |

---

> **Lưu ý**: Các nội dung Identity & Organization Core, Security & JWT, và Authorization & RBAC đã được tổ chức lại vào **[Phase 4 — Identity, Access & Multi-Tenancy](../phase-4/README.md)** theo đúng lộ trình.

---

> Xem thêm lộ trình tổng thể tại [Backend Roadmap](../backend-roadmap.md) và phạm vi MVP tại [MVP Scope & User Roles](../MVP-SCOPE-AND-USER-ROLES.md).
