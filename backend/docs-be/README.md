# E-Edu — Backend Architecture & Technical Documentation

Thư mục tài liệu kiến trúc, đặc tả kỹ thuật và lộ trình triển khai hệ thống Backend cho E-Edu Platform.

---

## Cấu trúc Thư mục Tài liệu

```text
backend/docs-be/
├── README.md                          # Mục lục tài liệu (file này)
├── backend-roadmap.md                 # Lộ trình triển khai tổng thể & checklist tiến độ
├── MVP-SCOPE-AND-USER-ROLES.md        # Phạm vi tính năng MVP, nhóm người dùng và quyền hạn
├── phase-1/                           # Phase 1: Kiến trúc tổng quan & Yêu cầu hệ thống
│   ├── README.md                      # Mục lục Phase 1
│   ├── phase1.1.md                    # Domain Model & Entity Relationships
│   ├── phase1.2.md                    # Module Boundaries & Dependency Rules
│   ├── phase1.3.md                    # Core Architecture Decisions (ADRs)
│   ├── phase1.4.md                    # Module Structure & Layering
│   ├── phase1.5.md                    # Persistence & Database Standards
│   ├── phase1.6.md                    # API Contracts & Response Standards
│   ├── phase1.7.md                    # Security & Authorization Model
│   └── phase1.8.md                    # Bootstrap, Configuration & Observability
└── phase-2/                           # Phase 2: Hạ tầng & Khung Bounded Contexts Cốt lõi
    ├── README.md                      # Mục lục Phase 2
    ├── phase2.1.md                    # Maven Multi-Module Project Setup
    ├── phase2.2.md                    # Spring Boot Bootstrap & Profiles
    ├── phase2.3.md                    # Docker Compose & PostgreSQL Multi-Schema
    ├── phase2.4.md                    # Shared Kernel & Domain Foundation
    ├── phase2.5/                      # Identity & Organization Core
    │   ├── README.md                  # Tổng quan & Điều hướng
    │   ├── 01-database-migrations.md  # V2..V4 SQL Flyway migrations
    │   ├── 02-jpa-entities-and-repositories.md
    │   ├── 03-organization-services.md
    │   ├── 04-identity-services.md
    │   ├── 05-rbac-and-tenant-isolation.md
    │   ├── 06-rest-api-controllers.md
    │   └── 07-archunit-and-verification.md
    └── phase2.6/                      # Security & JWT Token Management
        ├── README.md                  # Tổng quan & Checkpoints Index
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

## Lộ trình & Tài liệu Theo dõi

- **Lộ trình thực hiện chi tiết**: [backend-roadmap.md](backend-roadmap.md)
- **Đặc tả vai trò & phạm vi MVP**: [MVP-SCOPE-AND-USER-ROLES.md](MVP-SCOPE-AND-USER-ROLES.md)
- **Tài liệu Phase 1 (Kiến trúc)**: [phase-1/README.md](phase-1/README.md)
- **Tài liệu Phase 2 (Hạ tầng & Core Domain)**: [phase-2/README.md](phase-2/README.md)
