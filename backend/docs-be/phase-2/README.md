# Phase 2 — Project Setup & Core Domain Foundation

Tài liệu thiết lập dự án kỹ thuật, hạ tầng cơ sở dữ liệu và xây dựng các domain cốt lõi (Shared Kernel, Identity, Organization, Security & JWT) cho backend E-Edu.

---

## Danh mục Tài liệu Kỹ thuật

Tất cả các giai đoạn của Phase 2 đều đã được cấu trúc thành các thư mục độc lập với các tài liệu thành phần chi tiết:

| Phase | Phân hệ / Giai đoạn | Thư mục | Trạng thái | Mô tả trọng tâm |
| :---: | :--- | :---: | :---: | :--- |
| **2.1** | [Maven Multi-Module Project](phase2.1/README.md) | `phase2.1/` | ✅ Hoàn thành | Thiết lập khung dự án Maven Reactor 19 modules, dependency management và plugin compiler. |
| **2.2** | [Spring Boot Bootstrap & Profiles](phase2.2/README.md) | `phase2.2/` | ✅ Hoàn thành | Cấu hình Java 21 LTS, Spring Boot 3.4.3, các profile `local`, `test`, `prod` và smoke tests. |
| **2.3** | [Docker Compose & PostgreSQL Multi-Schema](phase2.3/README.md) | `phase2.3/` | ✅ Hoàn thành | Thiết lập container PostgreSQL 15, phân tách schemas độc lập và cấu hình Flyway migrations. |
| **2.4** | [Shared Kernel & Domain Foundation](phase2.4/README.md) | `phase2.4/` | ✅ Hoàn thành | Strongly typed IDs (`UserId`, `OrganizationId`), Base entities, `DomainEvent`, `ApiErrorResponse` và ArchUnit rules. |
| **2.5** | [Identity & Organization Core](phase2.5/README.md) | `phase2.5/` | ✅ Hoàn thành | Triển khai domain `identity` & `organization`, Public Port `OrganizationAccountQuery`, REST APIs, RBAC và ArchUnit tests.<br/>*(Thư mục gồm 7 tài liệu chi tiết từ migrations đến controllers).* |
| **2.6** | [Security & JWT Token Management](phase2.6/README.md) | `phase2.6/` | 🚧 Đang triển khai | Kiến trúc bảo mật stateless 3 tầng, Spring Security 6 filter chain, JWT access token, Refresh token rotation, tenant context.<br/>*(Thư mục gồm 9 checkpoints từ ADR đến integration tests).* |

---

## Cấu trúc Thư mục Phase 2

```text
phase-2/
├── README.md                                # Tài liệu tổng quan (file này)
├── phase2.1/                                # Maven Multi-Module Project Setup
│   ├── README.md
│   ├── 01-maven-multi-module-hierarchy.md
│   ├── 02-root-parent-pom-configuration.md
│   ├── 03-module-poms-and-dependencies.md
│   └── 04-build-verification-guide.md
├── phase2.2/                                # Spring Boot Bootstrap & Profiles
│   ├── README.md
│   ├── 01-spring-boot-application-entrypoint.md
│   ├── 02-application-profiles-and-configuration.md
│   ├── 03-configuration-properties-validation.md
│   └── 04-smoke-tests-and-verification.md
├── phase2.3/                                # PostgreSQL & Flyway Multi-Schema
│   ├── README.md
│   ├── 01-docker-compose-postgresql-setup.md
│   ├── 02-multi-schema-architecture-and-isolation.md
│   ├── 03-flyway-migration-management.md
│   ├── 04-datasource-and-connection-hardening.md
│   └── 05-verification-and-troubleshooting.md
├── phase2.4/                                # Shared Kernel & Domain Foundation
│   ├── README.md
│   ├── 01-strongly-typed-ids-and-primitives.md
│   ├── 02-auditable-entity-and-persistence-foundation.md
│   ├── 03-domain-events-contract-and-publisher.md
│   ├── 04-global-exception-handling-and-api-error-response.md
│   ├── 05-archunit-rules-and-architecture-tests.md
│   └── 06-verification-and-testing-guide.md
├── phase2.5/                                # Identity & Organization Core (Module chi tiết)
│   ├── README.md                            # Tổng quan, Kiến trúc & Điều hướng
│   ├── 01-database-migrations.md            # V2, V3, V4 SQL Flyway migrations
│   ├── 02-jpa-entities-and-repositories.md  # Entities & Repositories
│   ├── 03-organization-services.md          # Public Port, Organization & Membership Services
│   ├── 04-identity-services.md              # IdentityService & OrganizationAccountQueryAdapter
│   ├── 05-rbac-and-tenant-isolation.md      # Ma trận phân quyền & Multi-tenancy
│   ├── 06-rest-api-controllers.md           # REST API Controllers & DTOs
│   └── 07-archunit-and-verification.md      # ArchUnit rules & Verification Guide
└── phase2.6/                                # Security & JWT Management (Checkpoints chi tiết)
    ├── README.md                            # Tổng quan kiến trúc & Checkpoints index
    ├── checkpoint-2.6.1-architecture-and-adr.md
    ├── checkpoint-2.6.2-dependencies-and-config.md
    ├── checkpoint-2.6.3-authentication-api-login.md
    ├── checkpoint-2.6.4-jwt-access-token.md
    ├── checkpoint-2.6.5-refresh-token-management.md
    ├── checkpoint-2.6.6-security-filter-chain.md
    ├── checkpoint-2.6.7-tenant-resolution-rbac.md
    ├── checkpoint-2.6.8-integration-tests.md
    └── checkpoint-2.6.9-hardening-and-summary.md
```

---

> Xem thêm lộ trình tổng thể tại [Backend Roadmap](../backend-roadmap.md) và phạm vi MVP tại [MVP Scope & User Roles](../MVP-SCOPE-AND-USER-ROLES.md).
