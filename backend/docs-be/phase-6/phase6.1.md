# Phase 6.1 — Teaching Schedules & Assignments (Module `teaching`)

> **Module**: `teaching` | **Package**: `com.edu.teaching` | **Schema DB**: `teaching`

---

## 1. Mục Tiêu

Module `teaching` quản lý **phân công giảng dạy** và **thời khóa biểu**. Cho phép admin gán giáo viên vào môn học cụ thể trong một học kỳ, đồng thời tạo lịch học (ngày/giờ, phòng học) cho từng buổi dạy.

**Phạm vi**:
- Phân công giáo viên phụ trách môn học (`TeachingAssignment`)
- Tạo thời khóa biểu chi tiết (`Schedule`): ngày, giờ, phòng học, học kỳ
- Hỗ trợ phát hiện xung đột lịch (giáo viên hoặc phòng học bị double-book)
- Cung cấp public port để các module khác (`attendance`, `assessment`) tra cứu giáo viên phụ trách

---

## 2. Kiến Trúc Module

```
com.edu.teaching/
├── domain/
│   ├── TeachingAssignment.java
│   ├── Schedule.java
│   └── DayOfWeek.java                # Enum: MON, TUE, WED, THU, FRI, SAT, SUN
├── application/
│   ├── port/
│   │   ├── in/
│   │   │   ├── AssignTeacherUseCase.java
│   │   │   ├── CreateScheduleUseCase.java
│   │   │   └── GetTeacherAssignmentUseCase.java   # Public port
│   │   └── out/
│   │       ├── TeachingAssignmentRepository.java
│   │       └── ScheduleRepository.java
│   └── service/
│       ├── TeachingAssignmentService.java
│       └── ScheduleService.java
├── adapter/
│   ├── in/
│   │   └── web/
│   │       ├── TeachingAssignmentController.java
│   │       └── ScheduleController.java
│   └── out/
│       └── persistence/
│           ├── TeachingAssignmentEntity.java
│           ├── ScheduleEntity.java
│           ├── TeachingAssignmentJpaRepository.java
│           └── ScheduleJpaRepository.java
└── TeachingModule.java
```

---

## 3. Thiết Kế Entity

### 3.1 `TeachingAssignmentEntity`

```java
@Entity
@Table(schema = "teaching", name = "teaching_assignments",
       uniqueConstraints = @UniqueConstraint(
           columnNames = {"teacher_id", "subject_id", "semester_id"}
       ))
public class TeachingAssignmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "teacher_id", nullable = false)
    private UUID teacherId;            // Logical ref → people.teachers

    @Column(name = "subject_id", nullable = false)
    private UUID subjectId;            // Logical ref → curriculum.subjects

    @Column(name = "class_room_id", nullable = false)
    private UUID classRoomId;          // Logical ref → academic.class_rooms

    @Column(name = "semester_id", nullable = false)
    private UUID semesterId;           // Logical ref → academic.semesters

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "assigned_at", nullable = false)
    private Instant assignedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
```

### 3.2 `ScheduleEntity`

```java
@Entity
@Table(schema = "teaching", name = "schedules")
public class ScheduleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "assignment_id", nullable = false)
    private UUID assignmentId;         // FK trong cùng schema teaching

    @Column(name = "day_of_week", nullable = false)
    private String dayOfWeek;          // MON, TUE, WED, ...

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "room", nullable = false)
    private String room;               // Tên/mã phòng học

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
```

---

## 4. Flyway Migrations

### `V23__create_teaching_schema.sql`
```sql
CREATE SCHEMA IF NOT EXISTS teaching;
```

### `V24__create_teaching_assignments.sql`
```sql
CREATE TABLE teaching.teaching_assignments (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    teacher_id      UUID        NOT NULL,
    subject_id      UUID        NOT NULL,
    class_room_id   UUID        NOT NULL,
    semester_id     UUID        NOT NULL,
    organization_id UUID        NOT NULL,
    assigned_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_assignment_teacher_subject_semester
        UNIQUE (teacher_id, subject_id, semester_id)
);

CREATE INDEX idx_assignments_teacher   ON teaching.teaching_assignments(teacher_id);
CREATE INDEX idx_assignments_subject   ON teaching.teaching_assignments(subject_id);
CREATE INDEX idx_assignments_semester  ON teaching.teaching_assignments(semester_id);
CREATE INDEX idx_assignments_org       ON teaching.teaching_assignments(organization_id);
```

### `V25__create_teaching_schedules.sql`
```sql
CREATE TABLE teaching.schedules (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    assignment_id   UUID        NOT NULL REFERENCES teaching.teaching_assignments(id) ON DELETE CASCADE,
    day_of_week     VARCHAR(3)  NOT NULL CHECK (day_of_week IN ('MON','TUE','WED','THU','FRI','SAT','SUN')),
    start_time      TIME        NOT NULL,
    end_time        TIME        NOT NULL,
    room            VARCHAR(50) NOT NULL,
    effective_from  DATE        NOT NULL,
    effective_to    DATE,
    organization_id UUID        NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_schedule_times CHECK (end_time > start_time)
);

CREATE INDEX idx_schedules_assignment ON teaching.schedules(assignment_id);
CREATE INDEX idx_schedules_day_room   ON teaching.schedules(day_of_week, room);
```

---

## 5. REST API

### 5.1 Teaching Assignment Endpoints

| Method | Endpoint | Mô tả | Auth |
|--------|----------|-------|------|
| `GET`  | `/api/v1/organizations/{orgId}/teaching-assignments` | Danh sách phân công | `ROLE_ADMIN` |
| `POST` | `/api/v1/organizations/{orgId}/teaching-assignments` | Phân công giáo viên | `ROLE_ADMIN` |
| `GET`  | `/api/v1/organizations/{orgId}/teaching-assignments/{id}` | Chi tiết phân công | `ROLE_ADMIN`, `ROLE_TEACHER` |
| `DELETE` | `/api/v1/organizations/{orgId}/teaching-assignments/{id}` | Hủy phân công | `ROLE_ADMIN` |

### 5.2 Schedule Endpoints

| Method | Endpoint | Mô tả | Auth |
|--------|----------|-------|------|
| `GET`  | `/api/v1/organizations/{orgId}/schedules` | Thời khóa biểu (filter by semester, teacher, class) | `ROLE_ADMIN`, `ROLE_TEACHER` |
| `POST` | `/api/v1/organizations/{orgId}/schedules` | Tạo lịch học | `ROLE_ADMIN` |
| `PUT`  | `/api/v1/organizations/{orgId}/schedules/{scheduleId}` | Cập nhật lịch | `ROLE_ADMIN` |
| `DELETE` | `/api/v1/organizations/{orgId}/schedules/{scheduleId}` | Xóa lịch học | `ROLE_ADMIN` |

**Request Body — Tạo phân công**:
```json
{
  "teacherId": "550e8400-e29b-41d4-a716-446655440005",
  "subjectId": "550e8400-e29b-41d4-a716-446655440010",
  "classRoomId": "550e8400-e29b-41d4-a716-446655440020",
  "semesterId": "550e8400-e29b-41d4-a716-446655440030"
}
```

**Request Body — Tạo lịch học**:
```json
{
  "assignmentId": "550e8400-e29b-41d4-a716-446655440040",
  "dayOfWeek": "MON",
  "startTime": "07:30:00",
  "endTime": "09:00:00",
  "room": "A201",
  "effectiveFrom": "2024-09-02"
}
```

---

## 6. Phát Hiện Xung Đột Lịch

`ScheduleService` kiểm tra xung đột trước khi tạo lịch mới:

```java
// Kiểm tra giáo viên bị double-book
boolean hasTeacherConflict = scheduleRepository.existsConflict(
    teacherId, dayOfWeek, startTime, endTime, effectiveFrom, effectiveTo
);

// Kiểm tra phòng học bị double-book
boolean hasRoomConflict = scheduleRepository.existsRoomConflict(
    room, dayOfWeek, startTime, endTime, effectiveFrom, effectiveTo
);

if (hasTeacherConflict || hasRoomConflict) {
    throw new ScheduleConflictException("Xung đột lịch học");
}
```

---

## 7. Acceptance Criteria

| ID | Tiêu chí | Loại | Trạng thái |
|----|----------|------|------------|
| AC-6.1.1 | Phân công giáo viên cùng môn, cùng học kỳ lần 2 → `409 Conflict` | Functional | 📋 Kế hoạch |
| AC-6.1.2 | Tạo lịch học trùng giờ với giáo viên đó → `422 Unprocessable Entity` (conflict) | Validation | 📋 Kế hoạch |
| AC-6.1.3 | Tạo lịch học trùng phòng và giờ → `422 Unprocessable Entity` | Validation | 📋 Kế hoạch |
| AC-6.1.4 | `GET /schedules?semesterId=X&teacherId=Y` trả đúng thời khóa biểu | Functional | 📋 Kế hoạch |
| AC-6.1.5 | Giáo viên chỉ xem được lịch của mình | Security | 📋 Kế hoạch |
| AC-6.1.6 | Flyway V23–V25 chạy thành công | Infrastructure | 📋 Kế hoạch |
| AC-6.1.7 | `end_time <= start_time` → `400 Bad Request` | Validation | 📋 Kế hoạch |

---

## 8. Liên Kết

- ← **[Phase 5.1 — Students & Teachers](../phase-5/phase5.1.md)**
- ← **[Phase 5.2 — Academic Periods & Classes](../phase-5/phase5.2.md)**
- → **[Phase 6.2 — Attendance](phase6.2.md)**: Module `attendance` dùng `TeachingAssignment` để xác định buổi học
- → **[Phase 6.3 — Assessments & Grades](phase6.3.md)**: Chỉ giáo viên được phân công mới có quyền nhập điểm
