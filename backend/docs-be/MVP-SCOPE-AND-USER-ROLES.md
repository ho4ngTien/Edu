# E-Edu — MVP Scope and User Roles

## 1. Purpose

This document defines the initial Minimum Viable Product (MVP) scope, primary user groups, and core functional requirements of the E-Edu platform. It establishes clear boundaries for the first release to ensure that development focuses on essential educational workflows.

---

## 2. MVP Objectives

The initial MVP aims to provide a centralized educational management platform that supports:

- User authentication, multi-tenancy context, and role-based authorization.
- Organization and campus unit management.
- Student, teacher, and class section management.
- Curriculum and course management.
- Academic grading, assessment results, and rule evaluations.
- Attendance recording and tracking.
- Role-based access to educational resources and administrative functions.

The platform follows a Modular Monolith architecture using Java 21, Spring Boot 3.4+, PostgreSQL (multi-schema), and ArchUnit enforcement.

---

## 3. Primary User Roles

In accordance with Phase 1.7 Security Architecture, all roles are organization-scoped (`MembershipRole`) through `OrganizationMembership` rather than assigned as global privileges.

| Role Code | Role Name | Description | Core Responsibilities |
| :--- | :--- | :--- | :--- |
| `ORG_ADMIN` | Administrator | Manages institution configuration and personnel within an organization. | User membership, role assignments, academic structure configuration. |
| `TEACHER` | Teacher / Instructor | Manages assigned classes, courses, and academic activities. | Attendance tracking, grade entry, student progress monitoring. |
| `STUDENT` | Student / Learner | Accesses personal academic information and learning progress. | View courses, grade reports, attendance history, and notifications. |

### 3.1 Administrator (`ORG_ADMIN`)

- Manage member accounts and role assignments within the organization.
- Create and maintain academic periods (academic years, semesters).
- Configure courses, programs, classes, and course offerings.
- Monitor attendance and assessment overview across departments.

### 3.2 Teacher (`TEACHER`)

- View assigned classes, course sections, and enrolled students.
- Record and update session attendance.
- Enter, update, and publish assessment grades for authorized classes.
- Access teaching schedules and class rosters.

### 3.3 Student (`STUDENT`)

- View personal student profile information.
- View enrolled courses and class schedule.
- View published assessment results and final grade transcripts.
- View personal attendance summary and history.
- Access institution notifications.

---

## 4. MVP Functional Scope & Module Mapping

The functional scope directly aligns with the 11 backend domain modules defined in Phase 1.2 and Phase 1.4:

| Feature Area | MVP Features | Backend Module | Priority |
| :--- | :--- | :--- | :--- |
| Authentication | Login, logout, refresh token UPSERT, token revocation | `identity` | Must Have |
| Authorization | Organization-scoped RBAC, permission enforcement (`MembershipRole`) | `identity` | Must Have |
| Multi-Tenancy | Organization registration, branch/campus units, memberships | `organization` | Must Have |
| User & People | Person profile, student profile, teacher profile | `people` | Must Have |
| Curriculum | Academic programs, program versions, course catalog | `curriculum` | Must Have |
| Academic Structure | Academic periods (years/semesters), classes, course offerings | `academic` | Must Have |
| Enrollment | Student enrollment into courses and class sections | `enrollment` | Must Have |
| Teaching Assignment | Assigning teachers to course offerings and class sections | `teaching` | Must Have |
| Attendance | Session attendance recording (present, absent, excused) | `attendance` | Must Have |
| Assessment & Grading | Assessment categories, grade entry, basic formula evaluation | `assessment` / `rule-engine` | Must Have |
| Notifications | In-app event-driven notifications (enrollment, grade publication) | `notification` | Should Have |
| Basic LMS | Lesson listing, lecture notes, syllabus material attachments | `lms` | Should Have |
| Reporting | Academic summary and attendance percentage exports | `reporting` | Should Have |

---

## 5. Out of Scope for Initial MVP

The following capabilities are excluded from the initial release to ensure focus on core stability:

- Online payment processing and tuition invoicing.
- AI-driven learning path recommendations.
- Integrated video conferencing and real-time virtual classrooms.
- Advanced predictive analytics and longitudinal retention reports.
- Third-party SIS / LMS synchronization protocols (LTI, SCORM).
- Native iOS and Android mobile client builds.
- Dynamic drag-and-drop rule authoring UI.

---

## 6. Core Architectural & Business Rules

1. **Authentication Requirement**: Every client request accessing protected resources must supply a valid JWT Bearer token.
2. **Backend Enforcement**: Authorization and tenant isolation are enforced at the backend service and repository layers, never delegated to frontend clients.
3. **Tenant Boundary (`organization_id`)**: All domain records within an institution carry `organization_id`. Cross-tenant data leakage is strictly blocked.
4. **Scope-Based Data Access**:
   - Teachers may manage only class sections and assessments where they hold an active teaching assignment.
   - Students may query only their own profile, attendance, and grade records.
   - Administrators operate strictly within the organization where they hold `ORG_ADMIN` membership.
5. **Data Auditability**: Key domain tables maintain immutable audit timestamps (`created_at`, `updated_at`) and audit logs for sensitive operations (grading, role assignment).
6. **Modular Monolith Discipline**: Modules communicate strictly through public Java interfaces (`api/` facade) or asynchronous Application Events. Direct cross-module repository or entity references are strictly forbidden and guarded by ArchUnit.

---

## 7. MVP Acceptance Criteria

The MVP is functionally verified when:

- [ ] Users authenticate successfully, receive JWT tokens, and obtain correct claims.
- [ ] Administrators can create academic periods, courses, class sections, and assign students/teachers.
- [ ] Teachers can record session attendance and enter grades for assigned students.
- [ ] Students can view their personal enrollments, attendance status, and published grades.
- [ ] Cross-tenant data access attempts (e.g., IDOR on another organization's student or class) return HTTP 403 Forbidden.
- [ ] Core business workflows pass automated integration tests using Testcontainers PostgreSQL.
- [ ] Global exception handling, structured logging with correlation IDs, and Actuator health checks are operational.

---

## 8. Implementation Next Steps

To prepare for Phase 2 (Project Setup) and Phase 3 (Foundation):

1. **Finalize Module Baseline**: Lock the initial dependency graph (`identity`, `organization`, `people`, `curriculum`, `academic`, `enrollment`, `teaching`, `attendance`, `assessment`).
2. **Contract-First Facades**: Confirm public interface contracts in `api/` packages before implementing database entities.
3. **Multi-Module Initialization**: Generate Maven multi-module parent and sub-module POM configurations.
4. **Automated Verification**: Configure ArchUnit architecture tests in `e-edu-tests/architecture-tests` to continuously validate packaging and dependency rules.
