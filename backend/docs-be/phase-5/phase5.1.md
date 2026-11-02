# Phase 5.1 — Students & Teachers (Module `people`)

> **Module**: `people` | **Package**: `com.edu.people` | **Schema DB**: `people`

---

## 1. Mục Tiêu

Module `people` chịu trách nhiệm quản lý hồ sơ **học sinh** (`Student`) và **giáo viên** (`Teacher`) trong hệ thống giáo dục. Đây là module nghiệp vụ cốt lõi cung cấp danh tính học thuật cho người dùng đã đăng ký qua module `identity`.

**Phạm vi**:
- Tạo, cập nhật, truy vấn hồ sơ học sinh và giáo viên
- Liên kết hồ sơ với tài khoản người dùng (`user_id`) từ module `identity` (logical reference, không có FK qua schema)
- Gắn hồ sơ với tổ chức (`organization_id`) từ module `organization`
- Quản lý trạng thái hoạt động (ACTIVE / INACTIVE / SUSPENDED)

---

## 2. Kiến Trúc Module

```
com.edu.people/
├── domain/
│   ├── Student.java                  # Domain aggregate
│   ├── Teacher.java                  # Domain aggregate
│   ├── StudentStatus.java            # Enum: ACTIVE, INACTIVE, SUSPENDED
│   └── TeacherStatus.java            # Enum: ACTIVE, INACTIVE, ON_LEAVE
├── application/
│   ├── port/
│   │   ├── in/
│   │   │   ├── CreateStudentUseCase.java
│   │   │   ├── UpdateStudentUseCase.java
│   │   │   ├── CreateTeacherUseCase.java
│   │   │   └── UpdateTeacherUseCase.java
│   │   └── out/
│   │       ├── StudentRepository.java
│   │       └── TeacherRepository.java
│   └── service/
│       ├── StudentService.java
│       └── TeacherService.java
├── adapter/
│   ├── in/
│   │   └── web/
│   │       ├── StudentController.java
│   │       └── TeacherController.java
│   └── out/
│       └── persistence/
│           ├── StudentEntity.java
│           ├── TeacherEntity.java
│           ├── StudentJpaRepository.java
│           └── TeacherJpaRepository.java
└── PeopleModule.java                 # Module boundary marker
```

---

## 3. Thiết Kế Entity

### 3.1 `StudentEntity`

```java
@Entity
@Table(schema = "people", name = "students")
public class StudentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;                // Logical reference tới identity.users

    @Column(name = "student_code", length = 20, unique = true, nullable = false)
    private String studentCode;         // Ví dụ: "STU2024001"

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;        // Logical reference tới organization.organizations

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StudentStatus status;

    @Column(name = "enrolled_at")
    private Instant enrolledAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
```

**Giải thích thiết kế**:
- `user_id`: Không có JPA `@ManyToOne` sang schema `identity` để đảm bảo tách biệt module. Chỉ là logical reference.
- `student_code`: UNIQUE toàn hệ thống (có thể mở rộng UNIQUE per org bằng partial index).
- `enrolled_at`: Thời điểm học sinh chính thức nhập học, phân biệt với `created_at` (thời điểm tạo record).

### 3.2 `TeacherEntity`

```java
@Entity
@Table(schema = "people", name = "teachers")
public class TeacherEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "teacher_code", length = 20, unique = true, nullable = false)
    private String teacherCode;         // Ví dụ: "TCH2024001"

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "specialization")
    private String specialization;      // Chuyên ngành giảng dạy

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TeacherStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
```

---

## 4. Flyway Migrations

### `V10__create_people_schema.sql`
```sql
CREATE SCHEMA IF NOT EXISTS people;
```

### `V11__create_people_students.sql`
```sql
CREATE TABLE people.students (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID        NOT NULL,
    student_code    VARCHAR(20) NOT NULL UNIQUE,
    full_name       VARCHAR(255) NOT NULL,
    date_of_birth   DATE,
    organization_id UUID        NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    enrolled_at     TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_student_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED'))
);

CREATE INDEX idx_students_organization_id ON people.students(organization_id);
CREATE INDEX idx_students_user_id         ON people.students(user_id);
CREATE INDEX idx_students_status          ON people.students(status);
```

### `V12__create_people_teachers.sql`
```sql
CREATE TABLE people.teachers (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID        NOT NULL,
    teacher_code    VARCHAR(20) NOT NULL UNIQUE,
    full_name       VARCHAR(255) NOT NULL,
    specialization  VARCHAR(255),
    organization_id UUID        NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_teacher_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'ON_LEAVE'))
);

CREATE INDEX idx_teachers_organization_id ON people.teachers(organization_id);
CREATE INDEX idx_teachers_user_id         ON people.teachers(user_id);
```

---

## 5. REST API

### 5.1 Student Endpoints

| Method | Endpoint | Mô tả | Auth |
|--------|----------|-------|------|
| `GET`  | `/api/v1/organizations/{orgId}/students` | Danh sách học sinh của org | `ROLE_ADMIN`, `ROLE_TEACHER` |
| `POST` | `/api/v1/organizations/{orgId}/students` | Tạo hồ sơ học sinh mới | `ROLE_ADMIN` |
| `GET`  | `/api/v1/organizations/{orgId}/students/{studentId}` | Chi tiết học sinh | `ROLE_ADMIN`, `ROLE_TEACHER` |
| `PUT`  | `/api/v1/organizations/{orgId}/students/{studentId}` | Cập nhật hồ sơ | `ROLE_ADMIN` |
| `DELETE` | `/api/v1/organizations/{orgId}/students/{studentId}` | Vô hiệu hóa học sinh | `ROLE_ADMIN` |

**Request Body — Tạo học sinh**:
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "studentCode": "STU2024001",
  "fullName": "Nguyễn Văn An",
  "dateOfBirth": "2005-03-15",
  "enrolledAt": "2024-09-01T00:00:00Z"
}
```

### 5.2 Teacher Endpoints

| Method | Endpoint | Mô tả | Auth |
|--------|----------|-------|------|
| `GET`  | `/api/v1/organizations/{orgId}/teachers` | Danh sách giáo viên | `ROLE_ADMIN` |
| `POST` | `/api/v1/organizations/{orgId}/teachers` | Tạo hồ sơ giáo viên | `ROLE_ADMIN` |
| `GET`  | `/api/v1/organizations/{orgId}/teachers/{teacherId}` | Chi tiết giáo viên | `ROLE_ADMIN` |
| `PUT`  | `/api/v1/organizations/{orgId}/teachers/{teacherId}` | Cập nhật hồ sơ | `ROLE_ADMIN` |

---

## 6. Acceptance Criteria

| ID | Tiêu chí | Loại | Trạng thái |
|----|----------|------|------------|
| AC-5.1.1 | `POST /students` với `studentCode` đã tồn tại → trả về `409 Conflict` | Functional | 📋 Kế hoạch |
| AC-5.1.2 | `GET /students` chỉ trả về học sinh thuộc `orgId` trong path | Security | 📋 Kế hoạch |
| AC-5.1.3 | Tạo học sinh thành công → `status = ACTIVE`, `created_at` được set tự động | Functional | 📋 Kế hoạch |
| AC-5.1.4 | `userId` không tồn tại trong `identity` → trả về `422 Unprocessable Entity` | Validation | 📋 Kế hoạch |
| AC-5.1.5 | Flyway migration V10–V12 chạy thành công trên schema `people` | Infrastructure | 📋 Kế hoạch |
| AC-5.1.6 | `ROLE_TEACHER` không thể tạo/sửa/xóa hồ sơ học sinh | Security | 📋 Kế hoạch |
| AC-5.1.7 | Xóa mềm: DELETE không xóa record, chỉ set `status = INACTIVE` | Functional | 📋 Kế hoạch |

---

## 7. Liên Kết

- ← **[Phase 4.2 — Organization Management](../phase-4/phase4.2.md)**: `organization_id` tham chiếu sang module `organization`
- → **[Phase 5.2 — Academic Periods & Classes](phase5.2.md)**: `StudentEntity` được sử dụng trong enrollment và class assignment
