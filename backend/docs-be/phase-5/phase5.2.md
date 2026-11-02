# Phase 5.2 — Academic Periods & Classes (Module `academic`)

> **Module**: `academic` | **Package**: `com.edu.academic` | **Schema DB**: `academic`

---

## 1. Mục Tiêu

Module `academic` quản lý **cấu trúc thời gian học tập** bao gồm năm học (`AcademicYear`), học kỳ (`Semester`) và lớp học (`ClassRoom`). Đây là xương sống của lịch học thuật trong hệ thống, các module khác (`enrollment`, `teaching`, `attendance`) sẽ tham chiếu đến các entity này.

**Phạm vi**:
- Tạo và quản lý năm học, xác định thời gian bắt đầu/kết thúc
- Tổ chức năm học thành các học kỳ
- Tạo lớp học (ClassRoom) gắn với năm học và tổ chức
- Quản lý sức chứa lớp học (`capacity`)

---

## 2. Kiến Trúc Module

```
com.edu.academic/
├── domain/
│   ├── AcademicYear.java
│   ├── Semester.java
│   ├── ClassRoom.java
│   └── AcademicStatus.java           # Enum: UPCOMING, ACTIVE, CLOSED, ARCHIVED
├── application/
│   ├── port/
│   │   ├── in/
│   │   │   ├── CreateAcademicYearUseCase.java
│   │   │   ├── CreateSemesterUseCase.java
│   │   │   └── CreateClassRoomUseCase.java
│   │   └── out/
│   │       ├── AcademicYearRepository.java
│   │       ├── SemesterRepository.java
│   │       └── ClassRoomRepository.java
│   └── service/
│       ├── AcademicYearService.java
│       ├── SemesterService.java
│       └── ClassRoomService.java
├── adapter/
│   ├── in/
│   │   └── web/
│   │       ├── AcademicYearController.java
│   │       ├── SemesterController.java
│   │       └── ClassRoomController.java
│   └── out/
│       └── persistence/
│           ├── AcademicYearEntity.java
│           ├── SemesterEntity.java
│           ├── ClassRoomEntity.java
│           ├── AcademicYearJpaRepository.java
│           ├── SemesterJpaRepository.java
│           └── ClassRoomJpaRepository.java
└── AcademicModule.java
```

---

## 3. Thiết Kế Entity

### 3.1 `AcademicYearEntity`

```java
@Entity
@Table(schema = "academic", name = "academic_years")
public class AcademicYearEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;               // Ví dụ: "Năm học 2024-2025"

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AcademicStatus status;     // UPCOMING, ACTIVE, CLOSED, ARCHIVED

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
```

### 3.2 `SemesterEntity`

```java
@Entity
@Table(schema = "academic", name = "semesters")
public class SemesterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "academic_year_id", nullable = false)
    private UUID academicYearId;       // FK trong cùng schema academic

    @Column(name = "name", nullable = false)
    private String name;               // Ví dụ: "Học kỳ 1", "Học kỳ 2"

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
```

> [!NOTE]
> `academic_year_id` ở đây là FK trong cùng schema `academic`, vì vậy có thể dùng `@ManyToOne` + `@JoinColumn` JPA thông thường (không vi phạm module boundary).

### 3.3 `ClassRoomEntity`

```java
@Entity
@Table(schema = "academic", name = "class_rooms",
       uniqueConstraints = @UniqueConstraint(columnNames = {"code", "organization_id"}))
public class ClassRoomEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "code", nullable = false)
    private String code;               // UNIQUE per org, ví dụ: "12A1", "CNTT-K24"

    @Column(name = "name", nullable = false)
    private String name;               // Tên lớp hiển thị

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "academic_year_id", nullable = false)
    private UUID academicYearId;

    @Column(name = "capacity")
    private Integer capacity;          // Sĩ số tối đa

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
```

---

## 4. Flyway Migrations

### `V13__create_academic_schema.sql`
```sql
CREATE SCHEMA IF NOT EXISTS academic;
```

### `V14__create_academic_years.sql`
```sql
CREATE TABLE academic.academic_years (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(255) NOT NULL,
    start_date      DATE         NOT NULL,
    end_date        DATE         NOT NULL,
    organization_id UUID         NOT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'UPCOMING',
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_academic_year_dates   CHECK (end_date > start_date),
    CONSTRAINT chk_academic_year_status  CHECK (status IN ('UPCOMING','ACTIVE','CLOSED','ARCHIVED'))
);

CREATE INDEX idx_academic_years_org    ON academic.academic_years(organization_id);
CREATE INDEX idx_academic_years_status ON academic.academic_years(status);
```

### `V15__create_academic_semesters.sql`
```sql
CREATE TABLE academic.semesters (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    academic_year_id UUID         NOT NULL REFERENCES academic.academic_years(id) ON DELETE CASCADE,
    name             VARCHAR(100) NOT NULL,
    start_date       DATE         NOT NULL,
    end_date         DATE         NOT NULL,
    organization_id  UUID         NOT NULL,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_semester_dates CHECK (end_date > start_date)
);

CREATE INDEX idx_semesters_academic_year ON academic.semesters(academic_year_id);
CREATE INDEX idx_semesters_org           ON academic.semesters(organization_id);
```

### `V16__create_academic_classes.sql`
```sql
CREATE TABLE academic.class_rooms (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code             VARCHAR(50)  NOT NULL,
    name             VARCHAR(255) NOT NULL,
    organization_id  UUID         NOT NULL,
    academic_year_id UUID         NOT NULL REFERENCES academic.academic_years(id),
    capacity         INTEGER      CHECK (capacity > 0),
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_class_code_per_org UNIQUE (code, organization_id)
);

CREATE INDEX idx_class_rooms_org           ON academic.class_rooms(organization_id);
CREATE INDEX idx_class_rooms_academic_year ON academic.class_rooms(academic_year_id);
```

---

## 5. REST API

### 5.1 Academic Year Endpoints

| Method | Endpoint | Mô tả | Auth |
|--------|----------|-------|------|
| `GET`  | `/api/v1/organizations/{orgId}/academic-years` | Danh sách năm học | `ROLE_ADMIN`, `ROLE_TEACHER` |
| `POST` | `/api/v1/organizations/{orgId}/academic-years` | Tạo năm học mới | `ROLE_ADMIN` |
| `GET`  | `/api/v1/organizations/{orgId}/academic-years/{yearId}` | Chi tiết năm học | `ROLE_ADMIN`, `ROLE_TEACHER` |
| `PUT`  | `/api/v1/organizations/{orgId}/academic-years/{yearId}` | Cập nhật năm học | `ROLE_ADMIN` |
| `GET`  | `/api/v1/organizations/{orgId}/academic-years/{yearId}/semesters` | Danh sách học kỳ | `ROLE_ADMIN`, `ROLE_TEACHER` |
| `POST` | `/api/v1/organizations/{orgId}/academic-years/{yearId}/semesters` | Tạo học kỳ | `ROLE_ADMIN` |

### 5.2 ClassRoom Endpoints

| Method | Endpoint | Mô tả | Auth |
|--------|----------|-------|------|
| `GET`  | `/api/v1/organizations/{orgId}/classes` | Danh sách lớp học | `ROLE_ADMIN`, `ROLE_TEACHER` |
| `POST` | `/api/v1/organizations/{orgId}/classes` | Tạo lớp học | `ROLE_ADMIN` |
| `GET`  | `/api/v1/organizations/{orgId}/classes/{classId}` | Chi tiết lớp | `ROLE_ADMIN`, `ROLE_TEACHER` |
| `PUT`  | `/api/v1/organizations/{orgId}/classes/{classId}` | Cập nhật lớp | `ROLE_ADMIN` |

**Request Body — Tạo năm học**:
```json
{
  "name": "Năm học 2024-2025",
  "startDate": "2024-09-01",
  "endDate": "2025-06-30",
  "status": "UPCOMING"
}
```

**Request Body — Tạo lớp học**:
```json
{
  "code": "12A1",
  "name": "Lớp 12A1",
  "academicYearId": "550e8400-e29b-41d4-a716-446655440001",
  "capacity": 35
}
```

---

## 6. Acceptance Criteria

| ID | Tiêu chí | Loại | Trạng thái |
|----|----------|------|------------|
| AC-5.2.1 | `start_date >= end_date` khi tạo năm học → `400 Bad Request` | Validation | 📋 Kế hoạch |
| AC-5.2.2 | Tạo học kỳ với `academicYearId` không thuộc org → `403 Forbidden` | Security | 📋 Kế hoạch |
| AC-5.2.3 | `code` lớp học trùng trong cùng org → `409 Conflict` | Functional | 📋 Kế hoạch |
| AC-5.2.4 | Xóa `AcademicYear` → cascade xóa các `Semester` liên quan | Functional | 📋 Kế hoạch |
| AC-5.2.5 | `capacity` không hợp lệ (≤ 0) → `400 Bad Request` | Validation | 📋 Kế hoạch |
| AC-5.2.6 | Flyway V13–V16 chạy thành công, schema `academic` được tạo đúng | Infrastructure | 📋 Kế hoạch |
| AC-5.2.7 | `GET /classes` chỉ trả về lớp thuộc `orgId` hiện tại | Security | 📋 Kế hoạch |

---

## 7. Liên Kết

- ← **[Phase 5.1 — Students & Teachers](phase5.1.md)**: Học sinh sẽ được gán vào lớp học qua module `enrollment`
- → **[Phase 5.3 — Courses & Curricula](phase5.3.md)**: Lớp học liên kết với chương trình học
- → **[Phase 5.4 — Enrollments](phase5.4.md)**: `ClassRoom` và `Semester` được tham chiếu trong `EnrollmentEntity`
