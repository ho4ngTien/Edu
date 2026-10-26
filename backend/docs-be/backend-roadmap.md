# Backend Roadmap

## Phase 1 — Architecture & Requirements
- [x] Xác định phạm vi MVP và các nhóm người dùng ([MVP-SCOPE-AND-USER-ROLES.md](MVP-SCOPE-AND-USER-ROLES.md))
- [x] Xác định domain model tổng quát ([phase1.1](phase-1/phase1.1/README.md))
- [x] Phân chia module và dependency rules ([phase1.2](phase-1/phase1.2/README.md), [phase1.3](phase-1/phase1.3/README.md), [phase1.4](phase-1/phase1.4/README.md), [phase1.5](phase-1/phase1.5/README.md), [phase1.6](phase-1/phase1.6/README.md), [phase1.7](phase-1/phase1.7/README.md), [phase1.8](phase-1/phase1.8/README.md))
- [x] Chốt naming convention và API conventions ([phase1.5](phase-1/phase1.5/README.md), [phase1.6](phase-1/phase1.6/README.md))

## Phase 2 — Project Setup & Core Domain Foundation
- [x] Tạo Maven multi-module project ([phase2.1](phase-2/phase2.1/README.md))
- [x] Cấu hình Java 21, Spring Boot và Application Profiles ([phase2.2](phase-2/phase2.2/README.md))
- [x] Cấu hình Docker Compose, PostgreSQL và Flyway Multi-Schema ([phase2.3](phase-2/phase2.3/README.md))
- [x] Shared Kernel & Domain Foundation ([phase2.4](phase-2/phase2.4/README.md))
- [x] Identity & Organization Core ([phase2.5](phase-2/phase2.5/README.md))
- [ ] Security & JWT Token Management ([phase2.6](phase-2/phase2.6/README.md))

## Phase 3 — Foundation & Observability
- [x] Global exception handling & API error response ([phase2.4](phase-2/phase2.4/README.md))
- [x] Request validation và error response chuẩn hóa ([phase2.4](phase-2/phase2.4/README.md), [phase2.5](phase-2/phase2.5/README.md))
- [x] Flyway migrations đa schema (`identity`, `organization`) ([phase2.3](phase-2/phase2.3/README.md), [phase2.5](phase-2/phase2.5/README.md))
- [ ] Logging tập trung và correlation ID (`TraceIdFilter`)
- [ ] Actuator, metrics và health indicators mở rộng

## Phase 4 — Identity, Access & Multi-Tenancy
- [x] Users, tài khoản và mã hóa mật khẩu BCrypt ([phase2.5](phase-2/phase2.5/README.md))
- [x] Organizations và Organization Memberships ([phase2.5](phase-2/phase2.5/README.md))
- [x] Public Port Decoupling (`OrganizationAccountQuery`) ([phase2.5](phase-2/phase2.5/README.md))
- [ ] Spring Security 6 stateless filter chain & JWT Access/Refresh tokens ([phase2.6](phase-2/phase2.6/README.md))
- [ ] Authentication endpoints (`POST /api/v1/auth/login`, `POST /api/v1/auth/refresh`) ([phase2.6](phase-2/phase2.6/README.md))
- [ ] Tenant resolution context (`X-Organization-Id` header) & RBAC evaluation ([phase2.6](phase-2/phase2.6/README.md))

## Phase 5 — Education Core
- [ ] Students và teachers (`people` module)
- [ ] Academic periods và classes (`academic` module)
- [ ] Courses và curricula (`curriculum` module)
- [ ] Enrollments và learning groups (`enrollment` module)

## Phase 6 — Academic Operations
- [ ] Teaching schedules và assignment (`teaching` module)
- [ ] Attendance recording và tracking (`attendance` module)
- [ ] Assessments và grades (`assessment` module)
- [ ] Rule engine integration (`rule-engine` module)
- [ ] Completion và certification rules (`certification` module)

## Phase 7 — Quality & Delivery
- [x] Unit tests & WebMvc mock slice tests (`identity`, `organization`, `shared-kernel`)
- [x] Architecture tests với ArchUnit (9 rules kiểm soát ranh giới module & schema)
- [ ] Integration tests kết nối PostgreSQL test container
- [ ] OpenAPI / Swagger documentation
- [ ] CI/CD pipeline và containerization deployment
