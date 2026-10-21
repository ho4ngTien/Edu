# Backend Roadmap

## Phase 1 — Architecture & Requirements
- [x] Xác định phạm vi MVP và các nhóm người dùng ([MVP-SCOPE-AND-USER-ROLES.md](MVP-SCOPE-AND-USER-ROLES.md))
- [x] Xác định domain model tổng quát ([phase1.1.md](phase1.1.md))
- [x] Phân chia module và dependency rules ([phase1.2.md](phase1.2.md), [phase1.3.md](phase1.3.md), [phase1.4.md](phase1.4.md), [phase1.5.md](phase1.5.md), [phase1.6.md](phase1.6.md), [phase1.7.md](phase1.7.md), [phase1.8.md](phase1.8.md))
- [x] Chốt naming convention và API conventions ([phase1.5.md](phase1.5.md), [phase1.6.md](phase1.6.md))

## Phase 2 — Project Setup
- [x] Tạo Maven multi-module project ([phase2.1.md](phase2.1.md))
- [x] Cấu hình Java 21, Spring Boot và Application Profiles ([phase2.2.md](phase2.2.md))
- [x] Cấu hình Docker Compose, PostgreSQL và Flyway Multi-Schema ([phase2.3.md](phase2.3.md))
- [x] Shared Kernel & Domain Foundation ([phase2.4.md](phase2.4.md))
- [ ] Identity & Organization Core ([phase2.5.md](phase2.5.md))

## Phase 3 — Foundation
- [ ] Global exception handling
- [ ] Request validation và error response
- [ ] Logging và correlation ID
- [ ] Flyway migrations và base testing

## Phase 4 — Identity & Organization
- [ ] Authentication và authorization
- [ ] Users, roles và permissions
- [ ] Organizations và organizational units
- [ ] Tenant isolation và access control

## Phase 5 — Education Core
- [ ] Students và teachers
- [ ] Academic periods và classes
- [ ] Courses và curricula
- [ ] Enrollments và learning groups

## Phase 6 — Academic Operations
- [ ] Teaching schedules và attendance
- [ ] Assessments và grades
- [ ] Progress tracking
- [ ] Completion và certification rules

## Phase 7 — Quality & Delivery
- [ ] Unit tests và integration tests
- [ ] Architecture tests
- [ ] OpenAPI documentation
- [ ] CI/CD và deployment
