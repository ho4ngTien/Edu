# Phase 1 — Architecture & Requirements

Tài liệu thiết kế kiến trúc nền tảng và đặc tả yêu cầu tổng thể cho hệ thống Backend E-Edu (Modular Monolith, Java 21, Spring Boot 3.4, PostgreSQL Multi-Schema).

---

## Danh mục Tài liệu Đặc tả Chi tiết

Tất cả các giai đoạn của Phase 1 đều đã được cấu trúc thành các thư mục độc lập và phân chia thành các tài liệu thành phần chuyên biệt:

| STT | Phân hệ / Giai đoạn | Thư mục | Số tài liệu | Mô tả trọng tâm |
| :---: | :--- | :---: | :---: | :--- |
| **1.1** | [Domain Model & Entity Relationships](phase1.1/README.md) | `phase1.1/` | 6 tài liệu | Thiết kế 8 nhóm domain, mô hình Organization → Program → Learning, phân tách People vs Identity, mô hình đánh giá đa thang điểm, sơ đồ ERD khái niệm. |
| **1.2** | [Module Boundaries & Dependency Rules](phase1.2/README.md) | `phase1.2/` | 4 tài liệu | Phân định ranh giới 11 Bounded Contexts, đồ thị phụ thuộc một chiều DAG, cấm cyclic dependencies, chuẩn hóa Public Ports và Anti-Corruption Layer. |
| **1.3** | [Core Architecture Decisions (ADRs)](phase1.3/README.md) | `phase1.3/` | 5 tài liệu | Tổng hợp các quyết định kiến trúc cốt lõi: ADR-001 (Shared DB Multi-Tenancy), ADR-002 (Schema per Module), ADR-003 (Maven + ArchUnit), ADR-004 (Rule Engine) và phân tích trade-offs. |
| **1.4** | [Module Structure & Layering](phase1.4/README.md) | `phase1.4/` | 4 tài liệu | Cấu trúc phân tầng Clean/Hexagonal Architecture nội bộ (`api`, `application`, `domain`, `infrastructure`), quy tắc package visibility và danh mục module. |
| **1.5** | [Persistence & Database Standards](phase1.5/README.md) | `phase1.5/` | 4 tài liệu | Chuẩn đặt tên bảng/cột `snake_case`, quy chuẩn UUID, `TIMESTAMPTZ` (UTC), `NUMERIC`, phân chia 12 schemas vật lý, và chiến lược Flyway migrations. |
| **1.6** | [API Contracts & Cross-Module Communication](phase1.6/README.md) | `phase1.6/` | 4 tài liệu | Giao tiếp đồng bộ qua Public Facades, bất đồng bộ qua Domain Events (`AFTER_COMMIT`), Transactional Outbox Pattern và chuẩn hóa JSON `ApiErrorResponse`. |
| **1.7** | [Security Architecture & Authorization Model](phase1.7/README.md) | `phase1.7/` | 4 tài liệu | Kiến trúc bảo mật không trạng thái, phân quyền Organization-Scoped RBAC, Spring Security 6 Filter Chain, và cơ chế phân giải `TenantContext` chống IDOR. |
| **1.8** | [Bootstrap, Configuration & Observability](phase1.8/README.md) | `phase1.8/` | 4 tài liệu | Composition Root tại `boot`, Spring profiles `local`/`test`/`prod`, bảo vệ secrets, Structured Logging với Correlation ID `TraceIdFilter`, Actuator health checks. |

---

## Cấu trúc Thư mục Chi tiết Phase 1

```text
phase-1/
├── README.md                                    # Tài liệu tổng quan (file này)
├── phase1.1/                                    # Domain Model & Entity Relationships
│   ├── README.md
│   ├── 01-domain-scope-and-entities.md
│   ├── 02-organization-program-learning.md
│   ├── 03-people-and-identity-separation.md
│   ├── 04-assessment-and-grading-models.md
│   ├── 05-conceptual-erd-and-relationships.md
│   └── 06-database-conventions-and-multi-tenancy.md
├── phase1.2/                                    # Module Boundaries & Dependency Rules
│   ├── README.md
│   ├── 01-bounded-contexts-and-modules.md
│   ├── 02-dependency-graph-and-rules.md
│   ├── 03-communication-patterns-and-events.md
│   └── 04-anti-corruption-and-isolation.md
├── phase1.3/                                    # Core Architecture Decisions (ADRs)
│   ├── README.md
│   ├── 01-adr-001-shared-database-multi-tenancy.md
│   ├── 02-adr-002-postgresql-schema-per-module.md
│   ├── 03-adr-003-maven-multi-module-and-archunit.md
│   ├── 04-adr-004-generalized-rule-engine.md
│   └── 05-consequences-and-mitigations.md
├── phase1.4/                                    # Module Structure & Layering
│   ├── README.md
│   ├── 01-maven-reactor-and-packaging.md
│   ├── 02-clean-hexagonal-layering.md
│   ├── 03-package-visibility-and-public-api.md
│   └── 04-module-responsibilities-and-schemas.md
├── phase1.5/                                    # Persistence & Database Standards
│   ├── README.md
│   ├── 01-database-naming-and-typing-conventions.md
│   ├── 02-schema-separation-and-table-catalog.md
│   ├── 03-tenant-isolation-and-foreign-key-rules.md
│   └── 04-flyway-migration-strategy.md
├── phase1.6/                                    # API Contracts & Cross-Module Communication
│   ├── README.md
│   ├── 01-synchronous-public-facades.md
│   ├── 02-asynchronous-domain-events.md
│   ├── 03-transactional-outbox-pattern.md
│   └── 04-rest-api-response-and-error-contracts.md
├── phase1.7/                                    # Security Architecture & Authorization Model
│   ├── README.md
│   ├── 01-security-principles-and-threat-model.md
│   ├── 02-organization-scoped-rbac.md
│   ├── 03-security-filter-chain-and-pipeline.md
│   └── 04-tenant-context-resolution.md
└── phase1.8/                                    # Bootstrap, Configuration & Observability
    ├── README.md
    ├── 01-composition-root-bootstrap.md
    ├── 02-multi-environment-configuration.md
    ├── 03-observability-logging-and-tracing.md
    └── 04-health-checks-and-metrics.md
```

---

> Xem thêm lộ trình tổng thể tại [Backend Roadmap](../backend-roadmap.md) và phạm vi MVP tại [MVP Scope & User Roles](../MVP-SCOPE-AND-USER-ROLES.md).
