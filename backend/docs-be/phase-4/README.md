# Phase 4 — Identity, Access & Multi-Tenancy

Tài liệu kỹ thuật cho phân hệ định danh người dùng, kiểm soát truy cập và cô lập đa tổ chức (Multi-Tenancy) trên nền tảng E-Edu.

---

## Kiến trúc Tổng thể

Phân hệ này triển khai mô hình bảo mật **Defense-in-Depth 4 tầng**:

```
Tier 1: JWT Authentication (Spring Security OAuth2)
       ↓
Tier 2: Tenant Resolution (X-Organization-Id + Membership Check)
       ↓
Tier 3: Method-Level Authorization (@PreAuthorize)
       ↓
Tier 4: Resource-Level Authorization (SQL-level tenant filtering)
```

Hai module cốt lõi:
- **`identity`** — Quản lý tài khoản người dùng, xác thực, phát hành token
- **`organization`** — Quản lý tổ chức, tư cách thành viên, phân quyền theo tổ chức

---

## Danh mục Tài liệu Kỹ thuật

| Phase | Tài liệu | Trạng thái | Mô tả trọng tâm |
| :---: | :--- | :---: | :--- |
| **4.1** | [Users & BCrypt Hashing](phase4.1.md) | ✅ Hoàn thành | Entity `UserEntity`, Flyway V2, `IdentityService`, `BCryptPasswordEncoder`, ArchUnit AR-09 |
| **4.2** | [Organizations & Memberships](phase4.2.md) | ✅ Hoàn thành | Entity `OrganizationEntity` & `MembershipEntity`, Flyway V3 & V4, `OrganizationService`, `MembershipService` |
| **4.3** | [Public Port Decoupling](phase4.3.md) | ✅ Hoàn thành | Interface `OrganizationAccountQuery`, Adapter `IdentityAccountQueryAdapter`, ArchUnit AR-07 & AR-08 |
| **4.4** | [JWT Token Management](phase4.4.md) | ✅ Hoàn thành | Spring Security 6 Stateless Filter Chain, `NimbusJwtEncoder/Decoder`, Refresh Token Rotation, Flyway V5 |
| **4.5** | [Authentication Endpoints](phase4.5.md) | ✅ Hoàn thành | `POST /api/v1/auth/login`, `/refresh`, `/logout`; chống Timing Attack; `TokenResponse` contract |
| **4.6** | [Tenant Resolution Context](phase4.6.md) | ✅ Hoàn thành | `TenantResolutionFilter`, `TenantContext` (ThreadLocal), `OrganizationMembershipQuery` Port |
| **4.7** | [Dynamic RBAC & Authorization](phase4.7.md) | 🚧 Đang triển khai | Permission Catalog `DOMAIN_RESOURCE_ACTION`, Flyway V6, `@PreAuthorize`, Resource-Level anti-IDOR |

---

## Quyết định Kiến trúc Chủ yếu

| ADR | Chủ đề | Tóm tắt |
| :--- | :--- | :--- |
| ADR-013 | Stateless Auth & JWT | `SessionCreationPolicy.STATELESS`, JWT ký HMAC-SHA256 |
| ADR-014 | Refresh Token Rotation | Opaque Token 256-bit, lưu SHA-256 hash, Reuse Detection |
| ADR-015 | Organization-Scoped RBAC | `X-Organization-Id` header, xác minh membership qua Public Port |
| ADR-016 | Module Boundaries | identity / organization / boot — không import entity nội bộ chéo module |
| ADR-017 | Fine-Grained Permission Catalog | Quyền nguyên tử `DOMAIN_RESOURCE_ACTION`, không hardcode role trong logic |
| ADR-020 | Decoupled Resource Policy | Module sở hữu dữ liệu tự kiểm tra quyền resource-level |

---

> Xem thêm lộ trình tổng thể tại [Backend Roadmap](../backend-roadmap.md) và phạm vi MVP tại [MVP Scope & User Roles](../MVP-SCOPE-AND-USER-ROLES.md).
