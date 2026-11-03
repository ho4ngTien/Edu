# Phase 1 — Architecture & Requirements

Tài liệu thiết kế kiến trúc nền tảng và đặc tả yêu cầu tổng thể cho hệ thống Backend E-Edu (Modular Monolith, Java 21, Spring Boot 3.4, PostgreSQL Multi-Schema).

---

## Danh mục Tài liệu Đặc tả

| STT | Tài liệu | Mô tả trọng tâm |
| :---: | :--- | :--- |
| **1.1** | [Domain Model & Entity Relationships](phase1.1.md) | Thiết kế 8 nhóm domain, mô hình Organization → Program → Learning, quan hệ thực thể mức khái niệm. |
| **1.2** | [Module Boundaries & Dependency Rules](phase1.2.md) | Phân định ranh giới 12 Bounded Contexts, quy tắc phụ thuộc và ma trận giao tiếp giữa các module. |
| **1.3** | [Core Architecture Decisions (ADRs)](phase1.3.md) | Tổng hợp các quyết định kiến trúc then chốt (ADR-001 đến ADR-006): Modular Monolith, Multi-Schema, Public Ports. |
| **1.4** | [Module Structure & Layering](phase1.4.md) | Cấu trúc phân tầng nội bộ (Clean / Hexagonal Architecture): domain, application, infrastructure, web. |
| **1.5** | [Persistence & Database Standards](phase1.5.md) | Chuẩn đặt tên bảng/cột (`snake_case`), quy chuẩn UUID, auditing fields và phân tách database schema. |
| **1.6** | [API Contracts & Response Standards](phase1.6.md) | Định dạng REST API chuẩn, phân trang, lọc dữ liệu, cấu trúc ApiResponse và cơ chế phát sinh Domain Events. |
| **1.7** | [Security & Authorization Model](phase1.7.md) | Kiến trúc bảo mật stateless, phân quyền RBAC đa cấp độ (Platform vs Tenant) và mô hình cô lập dữ liệu tổ chức. |
| **1.8** | [Bootstrap, Configuration & Observability](phase1.8.md) | Cấu hình Spring Boot profiles, centralized logging với Correlation ID / Trace ID, Actuator & Health indicators. |

---

> Xem thêm lộ trình tổng thể tại [Backend Roadmap](../backend-roadmap.md) và phạm vi MVP tại [MVP Scope & User Roles](../MVP-SCOPE-AND-USER-ROLES.md).
