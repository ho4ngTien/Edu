# Backend Roadmap

## Phase 1 — Architecture & Requirements
- [x] Xác định phạm vi MVP và các nhóm người dùng ([MVP-SCOPE-AND-USER-ROLES.md](MVP-SCOPE-AND-USER-ROLES.md))
- [x] Xác định domain model tổng quát ([phase1.1](phase-1/phase1.1.md))
- [x] Phân chia module và dependency rules ([phase1.2](phase-1/phase1.2.md), [phase1.3](phase-1/phase1.3.md), [phase1.4](phase-1/phase1.4.md), [phase1.5](phase-1/phase1.5.md), [phase1.6](phase-1/phase1.6.md), [phase1.7](phase-1/phase1.7.md), [phase1.8](phase-1/phase1.8.md))
- [x] Chốt naming convention và API conventions ([phase1.5](phase-1/phase1.5.md), [phase1.6](phase-1/phase1.6.md))

## Phase 2 — Project Setup & Core Domain Foundation
- [x] Tạo Maven multi-module project ([phase2.1](phase-2/phase2.1.md))
- [x] Cấu hình Java 21, Spring Boot và Application Profiles ([phase2.2](phase-2/phase2.2.md))
- [x] Cấu hình Docker Compose, PostgreSQL và Flyway Multi-Schema ([phase2.3](phase-2/phase2.3.md))
- [x] Shared Kernel & Domain Foundation ([phase2.4](phase-2/phase2.4.md))

## Phase 3 — Foundation & Observability
- [x] Global exception handling & API error response ([phase3.1](phase-3/phase3.1.md))
- [x] Request validation và error response chuẩn hóa ([phase3.2](phase-3/phase3.2.md))
- [x] Flyway migrations đa schema (`identity`, `organization`) ([phase3.3](phase-3/phase3.3.md))
- [ ] Logging tập trung và correlation ID (`TraceIdFilter`) ([phase3.4](phase-3/phase3.4.md))
- [ ] Actuator, metrics và health indicators mở rộng ([phase3.5](phase-3/phase3.5.md))

## Phase 4 — Identity, Access & Multi-Tenancy
- [x] Users, tài khoản và mã hóa mật khẩu BCrypt ([phase4.1](phase-4/phase4.1.md))
- [x] Organizations và Organization Memberships ([phase4.2](phase-4/phase4.2.md))
- [x] Public Port Decoupling (`OrganizationAccountQuery`) ([phase4.3](phase-4/phase4.3.md))
- [x] Spring Security 6 stateless filter chain & JWT Access/Refresh tokens ([phase4.4](phase-4/phase4.4.md))
- [x] Authentication endpoints (`POST /api/v1/auth/login`, `POST /api/v1/auth/refresh`) ([phase4.5](phase-4/phase4.5.md))
- [x] Tenant resolution context (`X-Organization-Id` header) & RBAC evaluation ([phase4.6](phase-4/phase4.6.md))
- [ ] Dynamic RBAC Permission Catalog & Resource-Level Authorization ([phase4.7](phase-4/phase4.7.md))

## Phase 5 — Education Core
- [ ] Students và teachers (`people` module) ([phase5.1](phase-5/phase5.1.md))
- [ ] Academic periods và classes (`academic` module) ([phase5.2](phase-5/phase5.2.md))
- [ ] Courses và curricula (`curriculum` module) ([phase5.3](phase-5/phase5.3.md))
- [ ] Enrollments và learning groups (`enrollment` module) ([phase5.4](phase-5/phase5.4.md))

## Phase 6 — Academic Operations
- [ ] Teaching schedules và assignment (`teaching` module) ([phase6.1](phase-6/phase6.1.md))
- [ ] Attendance recording và tracking (`attendance` module) ([phase6.2](phase-6/phase6.2.md))
- [ ] Assessments và grades (`assessment` module) ([phase6.3](phase-6/phase6.3.md))
- [ ] Rule engine integration (`rule-engine` module) ([phase6.4](phase-6/phase6.4.md))
- [ ] Completion và certification rules (`certification` module) ([phase6.5](phase-6/phase6.5.md))

## Phase 7 — Quality & Delivery
- [x] Unit tests & WebMvc mock slice tests (`identity`, `organization`, `shared-kernel`) ([phase7.1](phase-7/phase7.1.md))
- [x] Architecture tests với ArchUnit (9 rules kiểm soát ranh giới module & schema) ([phase7.2](phase-7/phase7.2.md))
- [ ] Integration tests kết nối PostgreSQL test container ([phase7.3](phase-7/phase7.3.md))
- [ ] OpenAPI / Swagger documentation ([phase7.4](phase-7/phase7.4.md))
- [ ] CI/CD pipeline và containerization deployment ([phase7.5](phase-7/phase7.5.md))
