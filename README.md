# School Management System

> A modular monolith platform for educational institution management — handling identity, people, curriculum, academic scheduling, enrollment, teaching assignment, attendance, and assessment across role-based portals.

[![Java](https://img.shields.io/badge/Java_21-Spring_Boot_3.4-6DB33F?style=flat&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/Frontend-React_%2B_Vite-61DAFB?style=flat&logo=react&logoColor=white)](https://react.dev)
[![PostgreSQL](https://img.shields.io/badge/Database-PostgreSQL-4169E1?style=flat&logo=postgresql&logoColor=white)](https://www.postgresql.org)
[![Docker](https://img.shields.io/badge/Deploy-Docker-2496ED?style=flat&logo=docker&logoColor=white)](https://www.docker.com)
[![TypeScript](https://img.shields.io/badge/Language-TypeScript-3178C6?style=flat&logo=typescript&logoColor=white)](https://www.typescriptlang.org)
[![ArchUnit](https://img.shields.io/badge/ArchUnit-1.4.1-FF6B6B?style=flat)](https://www.archunit.org)

> 🚧 **Active Development** — Architecture design complete (Phase 1). Project Setup, Core Domain, Identity & Organization core, and Stateless Security & JWT completed (Phase 2). Foundation & Observability (Phase 3) and Identity, Access & Multi-Tenancy (Phase 4) in active implementation.

---

## Overview

School operations are fragmented across spreadsheets, paper forms, and disconnected tools. This platform centralizes everything: enrollment, scheduling, grade management, attendance tracking, and notifications — into a single deployable application accessible by students, teachers, and administrators.

Built on a **Modular Monolith** architecture — one Spring Boot application with 12 independently bounded modules, each owning its own database schema, communicating only through well-defined Java interfaces or Application Events.

---

## Architecture

### System Overview

```mermaid
graph TB
    subgraph Frontend["Frontend (React + Vite SPA)"]
        direction LR
        S["/student/*<br>Schedule · Grades · Attendance"]
        T["/teacher/*<br>Attendance · Grading · Classes"]
        A["/admin/*<br>Users · Config · Academic Structure"]
    end

    subgraph Backend["Backend (Spring Boot — Modular Monolith)"]
        direction TB
        identity["identity<br>JWT · RBAC"]
        organization["organization<br>Campus · Membership"]
        people["people<br>Student · Teacher profiles"]
        curriculum["curriculum<br>Programs · Courses"]
        academic["academic<br>Periods · ClassSections"]
        enrollment["enrollment<br>Course registration"]
        teaching["teaching<br>Assignment · Schedule"]
        attendance["attendance<br>Session check-in"]
        assessment["assessment<br>Grades · Transcripts"]
        rule_engine["rule-engine<br>Business rules"]
        configuration["configuration<br>Typed system config"]
        certification["certification<br>Graduation conditions"]
    end

    DB[("PostgreSQL<br>Multi-Schema")]

    Frontend -->|REST API + JWT| Backend
    Backend --> DB
```

### Backend Modules

| Module | Responsibility | Priority |
|:-------|:--------------|:--------:|
| `identity` | Authentication, JWT, RBAC, token management | Must Have |
| `organization` | Organization, campus units, memberships | Must Have |
| `people` | Person profile, student profile, teacher profile | Must Have |
| `curriculum` | Academic programs, program versions, course catalog | Must Have |
| `academic` | Academic periods, class sections, course offerings | Must Have |
| `enrollment` | Student enrollment into courses and class sections | Must Have |
| `teaching` | Teacher assignment to course offerings and classes | Must Have |
| `attendance` | Session attendance recording and tracking | Must Have |
| `assessment` | Grade entry, assessment categories, transcripts | Must Have |
| `rule-engine` | Versioned business rule evaluation | Must Have |
| `configuration` | Typed system configuration management | Should Have |
| `certification` | Graduation conditions and certification | Should Have |

### User Roles

All roles are **organization-scoped** — there are no global privileges.

| Role | Description |
|:-----|:------------|
| `ORG_ADMIN` | Manages institution configuration, academic structure, and personnel |
| `TEACHER` | Manages assigned classes, records attendance, enters grades |
| `STUDENT` | Views personal enrollments, grades, attendance, and schedule |

### Frontend Portals

One SPA with role-based routing after authentication:
- `/student/*` — View schedule, grades, attendance history
- `/teacher/*` — Record attendance, enter grades, view classes
- `/admin/*` — Manage users, configure academic structure

---

## Tech Stack

```
Frontend      React · TypeScript · Vite
              React Router · Axios · TailwindCSS

Backend       Java 21 · Spring Boot 3.4.3
              Spring Security · JWT
              Spring Data JPA · Hibernate
              PostgreSQL (per-module schema)
              ArchUnit 1.4.1 (architecture enforcement)

Build         Maven Multi-Module (reactor build)

Infrastructure  Docker + Docker Compose
                Nginx (reverse proxy)
```

---

## Project Structure

```
Edu/
├── backend/
│   ├── pom.xml                          # Root parent POM (com.edu, Java 21)
│   ├── boot/                            # Composition Root — EduApplication
│   ├── shared/
│   │   └── shared-kernel/               # Base domain abstractions
│   ├── modules/                         # 12 business modules
│   │   ├── identity/
│   │   ├── organization/
│   │   ├── configuration/
│   │   ├── people/
│   │   ├── curriculum/
│   │   ├── academic/
│   │   ├── enrollment/
│   │   ├── teaching/
│   │   ├── attendance/
│   │   ├── assessment/
│   │   ├── certification/
│   │   └── rule-engine/
│   ├── tests/
│   │   └── architecture-tests/          # ArchUnit enforcement
│   └── docs-be/                         # Architecture documentation
├── frontend/
│   └── src/
│       ├── features/                    # Feature-based modules
│       └── shared/                      # API client · shared components
├── infrastructure/
│   ├── docker-compose.yml
│   └── nginx/
└── docs/
```

---

## Quick Start

### Prerequisites

- Java 21+
- Maven 3.9+
- Node.js 20+
- Docker + Docker Compose

### Run

```bash
# Clone
git clone https://github.com/ho4ngTien/Edu.git
cd Edu
git checkout develop

# Start database
cd infrastructure && docker-compose up -d

# Backend — build and run
cd ../backend
mvn clean verify          # verify full build (19 modules)
mvn -pl boot -am spring-boot:run

# Frontend
cd ../frontend && npm install && npm run dev
```

---

## Development Status

### Backend

| Phase / Area | Status | Notes |
|:-------|:------:|:------|
| **Phase 1** — Architecture & Requirements | ✅ Complete | Domain model, bounded contexts, module rules, ADR-001–004 |
| **Phase 2** — Project Setup & Core Domain Foundation | ✅ Complete | Maven multi-module (19 modules), Spring Boot 3.4.3, PostgreSQL multi-schema, Shared Kernel, Identity & Org core, Security & JWT |
| **Phase 3** — Foundation & Observability | 🚧 In Progress | Global exception handling, validation, Flyway multi-schema complete; centralized logging & Actuator metrics in progress |
| **Phase 4** — Identity, Access & Multi-Tenancy | 🚧 In Progress | Users & BCrypt, Org memberships, Public ports, Stateless JWT, Tenant resolution complete; dynamic RBAC catalog in progress |
| **Phase 5** — Education Core | ⏳ Planned | `people` (students, teachers), `academic` (periods, classes), `curriculum` (courses), `enrollment` |
| **Phase 6** — Academic Operations | ⏳ Planned | `teaching` (schedules), `attendance`, `assessment` (grades), `rule-engine`, `certification` |
| **Phase 7** — Quality & Delivery | 🚧 In Progress | Unit tests & WebMvc slice tests, 9 ArchUnit rules complete; Testcontainers, OpenAPI, CI/CD planned |

### Frontend

| Feature | Status |
|:--------|:------:|
| Auth flow (login / token) | 🚧 In Progress |
| Student portal | 🚧 In Progress |
| Teacher portal | ⏳ Planned |
| Admin portal | ⏳ Planned |

---

## Documentation

Architecture and implementation specifications in `backend/docs-be/`:

| Document | Content |
|:---------|:--------|
| [MVP Scope & User Roles](backend/docs-be/MVP-SCOPE-AND-USER-ROLES.md) | Feature scope, user roles, acceptance criteria |
| [Backend Roadmap](backend/docs-be/backend-roadmap.md) | Implementation milestones, phase breakdown and checklist |
| [Phase 1 Overview](backend/docs-be/phase-1/README.md) | Architecture & Requirements (Domain model, Bounded Contexts, ADRs) |
| [Phase 2 Overview](backend/docs-be/phase-2/README.md) | Project Setup & Core Domain (Maven multi-module, Flyway, Shared Kernel, Identity, Security) |
| [Phase 3 Overview](backend/docs-be/phase-3/README.md) | Foundation & Observability (Global Exception Handling, Validation, Flyway, Logging, Actuator) |
| [Phase 4 Overview](backend/docs-be/phase-4/README.md) | Identity, Access & Multi-Tenancy (Users, Org Memberships, JWT, Tenant Resolution, RBAC) |
| [Phase 5 Overview](backend/docs-be/phase-5/README.md) | Education Core (Students, Academic Periods, Curriculum, Enrollment) |
| [Phase 6 Overview](backend/docs-be/phase-6/README.md) | Academic Operations (Teaching, Attendance, Assessments, Rule Engine, Certification) |
| [Phase 7 Overview](backend/docs-be/phase-7/README.md) | Quality & Delivery (Unit tests, ArchUnit 9 rules, Testcontainers, OpenAPI, CI/CD) |
