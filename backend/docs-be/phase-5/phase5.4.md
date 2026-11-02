# Phase 5.4 — Enrollments & Learning Groups (Module `enrollment`)

> **Module**: `enrollment` | **Package**: `com.edu.enrollment` | **Schema DB**: `enrollment`

---

## 1. Mục Tiêu

Module `enrollment` quản lý **đăng ký học phần** của học sinh vào môn học cụ thể trong một học kỳ, lớp học nhất định. Đây là điểm giao thoa giữa hồ sơ học sinh (`people`), cấu trúc học thuật (`academic`) và nội dung môn học (`curriculum`).

**Phạm vi**:
- Học sinh đăng ký môn học → tạo `Enrollment` record
- Quản lý vòng đời đăng ký: ENROLLED → COMPLETED / WITHDRAWN
- Tổ chức học sinh vào nhóm học tập (`LearningGroup`) trong lớp
- Kiểm tra giới hạn sĩ số khi đăng ký

---

## 2. Kiến Trúc Module

```
com.edu.enrollment/
├── domain/
│   ├── Enrollment.java
│   ├── LearningGroup.java
│   ├── EnrollmentStatus.java         # Enum: ENROLLED, WITHDRAWN, COMPLETED
│   └── EnrollmentPolicy.java         # Policy: kiểm tra capacity, điều kiện tiên quyết
├── application/
│   ├── port/
│   │   ├── in/
│   │   │   ├── EnrollStudentUseCase.java
│   │   │   ├── WithdrawEnrollmentUseCase.java
│   │   │   ├── CompleteEnrollmentUseCase.java
│   │   │   └── CreateLearningGroupUseCase.java
│   │   └── out/
│   │       ├── EnrollmentRepository.java
│   │       ├── LearningGroupRepository.java
│   │       └── ClassRoomCapacityPort.java    # Port để query capacity từ academic module
│   └── service/
│       ├── EnrollmentService.java
│       └── LearningGroupService.java
├── adapter/
│   ├── in/
│   │   └── web/
│   │       ├── EnrollmentController.java
│   │       └── LearningGroupController.java
│   └── out/
│       └── persistence/
│           ├── EnrollmentEntity.java
│           ├── LearningGroupEntity.java
│           ├── EnrollmentJpaRepository.java
│           └── LearningGroupJpaRepository.java
└── EnrollmentModule.java
```

---

## 3. Thiết Kế Entity

### 3.1 `EnrollmentEntity`

```java
@Entity
@Table(schema = "enrollment", name = "enrollments",
       uniqueConstraints = @UniqueConstraint(
           columnNames = {"student_id", "subject_id", "semester_id"}
       ))
public class EnrollmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;            // Logical ref → people.students

    @Column(name = "subject_id", nullable = false)
    private UUID subjectId;            // Logical ref → curriculum.subjects

    @Column(name = "class_room_id", nullable = false)
    private UUID classRoomId;          // Logical ref → academic.class_rooms

    @Column(name = "semester_id", nullable = false)
    private UUID semesterId;           // Logical ref → academic.semesters

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EnrollmentStatus status;   // ENROLLED, WITHDRAWN, COMPLETED

    @Column(name = "enrolled_at", nullable = false)
    private Instant enrolledAt;

    @Column(name = "withdrawn_at")
    private Instant withdrawnAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
```

**Giải thích thiết kế**:
- `UNIQUE (student_id, subject_id, semester_id)`: Học sinh chỉ được đăng ký một môn một lần trong một học kỳ.
- `withdrawn_at`, `completed_at`: Audit timestamps để theo dõi lịch sử thay đổi trạng thái.
- Không có FK sang các schema khác — tất cả là logical references.

### 3.2 `LearningGroupEntity`

```java
@Entity
@Table(schema = "enrollment", name = "learning_groups")
public class LearningGroupEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "class_room_id", nullable = false)
    private UUID classRoomId;          // Logical ref → academic.class_rooms

    @Column(name = "name", nullable = false)
    private String name;               // Ví dụ: "Nhóm 1", "Nhóm Thực Hành A"

    @Column(name = "description")
    private String description;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "max_members")
    private Integer maxMembers;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
```

---

## 4. Flyway Migrations

### `V20__create_enrollment_schema.sql`
```sql
CREATE SCHEMA IF NOT EXISTS enrollment;
```

### `V21__create_enrollment_enrollments.sql`
```sql
CREATE TABLE enrollment.enrollments (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id      UUID        NOT NULL,
    subject_id      UUID        NOT NULL,
    class_room_id   UUID        NOT NULL,
    semester_id     UUID        NOT NULL,
    organization_id UUID        NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'ENROLLED',
    enrolled_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    withdrawn_at    TIMESTAMPTZ,
    completed_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_enrollment_student_subject_semester
        UNIQUE (student_id, subject_id, semester_id),
    CONSTRAINT chk_enrollment_status
        CHECK (status IN ('ENROLLED', 'WITHDRAWN', 'COMPLETED'))
);

CREATE INDEX idx_enrollments_student      ON enrollment.enrollments(student_id);
CREATE INDEX idx_enrollments_subject      ON enrollment.enrollments(subject_id);
CREATE INDEX idx_enrollments_class_room   ON enrollment.enrollments(class_room_id);
CREATE INDEX idx_enrollments_semester     ON enrollment.enrollments(semester_id);
CREATE INDEX idx_enrollments_org_status   ON enrollment.enrollments(organization_id, status);
```

### `V22__create_enrollment_learning_groups.sql`
```sql
CREATE TABLE enrollment.learning_groups (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    class_room_id   UUID         NOT NULL,
    name            VARCHAR(100) NOT NULL,
    description     TEXT,
    organization_id UUID         NOT NULL,
    max_members     INTEGER      CHECK (max_members > 0),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_learning_groups_class_room ON enrollment.learning_groups(class_room_id);
CREATE INDEX idx_learning_groups_org        ON enrollment.learning_groups(organization_id);
```

---

## 5. REST API

### 5.1 Enrollment Endpoints

| Method | Endpoint | Mô tả | Auth |
|--------|----------|-------|------|
| `GET`  | `/api/v1/organizations/{orgId}/enrollments` | Danh sách đăng ký (filter by student/semester) | `ROLE_ADMIN`, `ROLE_TEACHER` |
| `POST` | `/api/v1/organizations/{orgId}/enrollments` | Đăng ký môn học cho học sinh | `ROLE_ADMIN` |
| `GET`  | `/api/v1/organizations/{orgId}/enrollments/{id}` | Chi tiết đăng ký | `ROLE_ADMIN` |
| `POST` | `/api/v1/organizations/{orgId}/enrollments/{id}/withdraw` | Rút khỏi môn học | `ROLE_ADMIN` |
| `POST` | `/api/v1/organizations/{orgId}/enrollments/{id}/complete` | Hoàn thành môn học | `ROLE_ADMIN` |
| `GET`  | `/api/v1/organizations/{orgId}/students/{studentId}/enrollments` | Tất cả đăng ký của học sinh | `ROLE_ADMIN`, `ROLE_TEACHER` |

### 5.2 Learning Group Endpoints

| Method | Endpoint | Mô tả | Auth |
|--------|----------|-------|------|
| `GET`  | `/api/v1/organizations/{orgId}/classes/{classId}/learning-groups` | Danh sách nhóm học | `ROLE_ADMIN`, `ROLE_TEACHER` |
| `POST` | `/api/v1/organizations/{orgId}/classes/{classId}/learning-groups` | Tạo nhóm học tập | `ROLE_ADMIN` |
| `PUT`  | `/api/v1/organizations/{orgId}/classes/{classId}/learning-groups/{groupId}` | Cập nhật nhóm | `ROLE_ADMIN` |

**Request Body — Đăng ký môn học**:
```json
{
  "studentId": "550e8400-e29b-41d4-a716-446655440001",
  "subjectId": "550e8400-e29b-41d4-a716-446655440010",
  "classRoomId": "550e8400-e29b-41d4-a716-446655440020",
  "semesterId": "550e8400-e29b-41d4-a716-446655440030",
  "enrolledAt": "2024-09-01T08:00:00Z"
}
```

---

## 6. Luồng Nghiệp Vụ (Enrollment Lifecycle)

```
[ENROLLED] ──withdraw──► [WITHDRAWN]
    │
    └──complete──► [COMPLETED]
```

- **ENROLLED**: Học sinh đã đăng ký thành công, đang tham gia khóa học
- **WITHDRAWN**: Học sinh đã rút khỏi môn học (có thể do học sinh tự rút hoặc admin thao tác)
- **COMPLETED**: Học sinh đã hoàn thành môn học (được trigger sau khi tính điểm xong)

> [!IMPORTANT]
> Trạng thái `WITHDRAWN` là cuối cùng — không thể chuyển lại `ENROLLED` cho cùng enrollment record. Học sinh muốn học lại phải tạo enrollment mới.

---

## 7. Acceptance Criteria

| ID | Tiêu chí | Loại | Trạng thái |
|----|----------|------|------------|
| AC-5.4.1 | Đăng ký trùng (cùng student, subject, semester) → `409 Conflict` | Functional | 📋 Kế hoạch |
| AC-5.4.2 | Rút khỏi enrollment có `status = COMPLETED` → `422 Unprocessable Entity` | Validation | 📋 Kế hoạch |
| AC-5.4.3 | `GET /enrollments?studentId=X&semesterId=Y` trả đúng danh sách đăng ký | Functional | 📋 Kế hoạch |
| AC-5.4.4 | Số học sinh trong lớp vượt quá `capacity` → `422 Unprocessable Entity` | Validation | 📋 Kế hoạch |
| AC-5.4.5 | Withdrawal thành công → `withdrawn_at` được set, `status = WITHDRAWN` | Functional | 📋 Kế hoạch |
| AC-5.4.6 | Chỉ học sinh của đúng `orgId` mới được đăng ký | Security | 📋 Kế hoạch |
| AC-5.4.7 | Flyway V20–V22 chạy thành công | Infrastructure | 📋 Kế hoạch |

---

## 8. Liên Kết

- ← **[Phase 5.1 — Students & Teachers](phase5.1.md)**: `student_id` tham chiếu `people.students`
- ← **[Phase 5.2 — Academic Periods & Classes](phase5.2.md)**: `class_room_id`, `semester_id`
- ← **[Phase 5.3 — Courses & Curricula](phase5.3.md)**: `subject_id` tham chiếu `curriculum.subjects`
- → **[Phase 6.2 — Attendance](../phase-6/phase6.2.md)**: Điểm danh dựa trên enrollment
- → **[Phase 6.3 — Assessments & Grades](../phase-6/phase6.3.md)**: Điểm số gắn với enrollment
