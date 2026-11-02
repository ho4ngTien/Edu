# Phase 5.3 — Courses & Curricula (Module `curriculum`)

> **Module**: `curriculum` | **Package**: `com.edu.curriculum` | **Schema DB**: `curriculum`

---

## 1. Mục Tiêu

Module `curriculum` quản lý **nội dung học tập**: môn học (`Subject`), chương trình đào tạo (`Curriculum`) và mối quan hệ giữa chúng. Đây là nền tảng để học sinh đăng ký học phần và để hệ thống xác nhận điều kiện hoàn thành chương trình.

**Phạm vi**:
- Định nghĩa môn học (mã môn, tên, số tín chỉ)
- Tạo chương trình đào tạo tổng hợp nhiều môn học
- Xác định môn bắt buộc / tự chọn trong chương trình
- Thiết lập thứ tự học môn (`sequence_order`)
- Hỗ trợ tra cứu môn học theo tổ chức

---

## 2. Kiến Trúc Module

```
com.edu.curriculum/
├── domain/
│   ├── Subject.java
│   ├── Curriculum.java
│   ├── CurriculumSubject.java        # Value Object liên kết
│   └── SubjectStatus.java            # Enum: ACTIVE, DEPRECATED
├── application/
│   ├── port/
│   │   ├── in/
│   │   │   ├── CreateSubjectUseCase.java
│   │   │   ├── CreateCurriculumUseCase.java
│   │   │   └── AssignSubjectToCurriculumUseCase.java
│   │   └── out/
│   │       ├── SubjectRepository.java
│   │       └── CurriculumRepository.java
│   └── service/
│       ├── SubjectService.java
│       └── CurriculumService.java
├── adapter/
│   ├── in/
│   │   └── web/
│   │       ├── SubjectController.java
│   │       └── CurriculumController.java
│   └── out/
│       └── persistence/
│           ├── SubjectEntity.java
│           ├── CurriculumEntity.java
│           ├── CurriculumSubjectEntity.java
│           ├── SubjectJpaRepository.java
│           └── CurriculumJpaRepository.java
└── CurriculumModule.java
```

---

## 3. Thiết Kế Entity

### 3.1 `SubjectEntity`

```java
@Entity
@Table(schema = "curriculum", name = "subjects",
       uniqueConstraints = @UniqueConstraint(columnNames = {"code", "organization_id"}))
public class SubjectEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "code", nullable = false)
    private String code;               // Mã môn, UNIQUE per org. Ví dụ: "MATH101"

    @Column(name = "name", nullable = false)
    private String name;               // Tên môn học

    @Column(name = "description")
    private String description;        // Mô tả môn học

    @Column(name = "credits", nullable = false)
    private Integer credits;           // Số tín chỉ

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SubjectStatus status;      // ACTIVE, DEPRECATED

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
```

### 3.2 `CurriculumEntity`

```java
@Entity
@Table(schema = "curriculum", name = "curricula")
public class CurriculumEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "code", nullable = false)
    private String code;               // Mã chương trình. Ví dụ: "CNTT-2024"

    @Column(name = "name", nullable = false)
    private String name;               // Tên chương trình

    @Column(name = "description")
    private String description;

    @Column(name = "total_credits", nullable = false)
    private Integer totalCredits;      // Tổng số tín chỉ yêu cầu

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "curriculum", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CurriculumSubjectEntity> curriculumSubjects = new ArrayList<>();
}
```

### 3.3 `CurriculumSubjectEntity` (Join Table với dữ liệu bổ sung)

```java
@Entity
@Table(schema = "curriculum", name = "curriculum_subjects")
@IdClass(CurriculumSubjectId.class)
public class CurriculumSubjectEntity {

    @Id
    @Column(name = "curriculum_id")
    private UUID curriculumId;

    @Id
    @Column(name = "subject_id")
    private UUID subjectId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curriculum_id", insertable = false, updatable = false)
    private CurriculumEntity curriculum;

    @Column(name = "is_required", nullable = false)
    private Boolean isRequired;        // true = bắt buộc, false = tự chọn

    @Column(name = "sequence_order")
    private Integer sequenceOrder;     // Thứ tự học môn trong chương trình

    @Column(name = "assigned_at", nullable = false, updatable = false)
    private Instant assignedAt;
}
```

---

## 4. Flyway Migrations

### `V17__create_curriculum_schema.sql`
```sql
CREATE SCHEMA IF NOT EXISTS curriculum;
```

### `V18__create_curriculum_subjects.sql`
```sql
CREATE TABLE curriculum.subjects (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code            VARCHAR(50)  NOT NULL,
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    credits         INTEGER      NOT NULL CHECK (credits > 0),
    organization_id UUID         NOT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_subject_code_per_org UNIQUE (code, organization_id),
    CONSTRAINT chk_subject_status CHECK (status IN ('ACTIVE', 'DEPRECATED'))
);

CREATE INDEX idx_subjects_org    ON curriculum.subjects(organization_id);
CREATE INDEX idx_subjects_status ON curriculum.subjects(status);
```

### `V19__create_curriculum_curricula.sql`
```sql
CREATE TABLE curriculum.curricula (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code            VARCHAR(50)  NOT NULL,
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    total_credits   INTEGER      NOT NULL CHECK (total_credits > 0),
    organization_id UUID         NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_curriculum_code_per_org UNIQUE (code, organization_id)
);

CREATE TABLE curriculum.curriculum_subjects (
    curriculum_id  UUID    NOT NULL REFERENCES curriculum.curricula(id) ON DELETE CASCADE,
    subject_id     UUID    NOT NULL REFERENCES curriculum.subjects(id)  ON DELETE RESTRICT,
    is_required    BOOLEAN NOT NULL DEFAULT true,
    sequence_order INTEGER,
    assigned_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    PRIMARY KEY (curriculum_id, subject_id)
);

CREATE INDEX idx_curriculum_subjects_curriculum ON curriculum.curriculum_subjects(curriculum_id);
CREATE INDEX idx_curriculum_subjects_subject    ON curriculum.curriculum_subjects(subject_id);
```

---

## 5. REST API

### 5.1 Subject Endpoints

| Method | Endpoint | Mô tả | Auth |
|--------|----------|-------|------|
| `GET`  | `/api/v1/organizations/{orgId}/subjects` | Danh sách môn học | `ROLE_ADMIN`, `ROLE_TEACHER` |
| `POST` | `/api/v1/organizations/{orgId}/subjects` | Tạo môn học mới | `ROLE_ADMIN` |
| `GET`  | `/api/v1/organizations/{orgId}/subjects/{subjectId}` | Chi tiết môn học | `ROLE_ADMIN`, `ROLE_TEACHER` |
| `PUT`  | `/api/v1/organizations/{orgId}/subjects/{subjectId}` | Cập nhật môn học | `ROLE_ADMIN` |
| `DELETE` | `/api/v1/organizations/{orgId}/subjects/{subjectId}` | Deprecated môn học | `ROLE_ADMIN` |

### 5.2 Curriculum Endpoints

| Method | Endpoint | Mô tả | Auth |
|--------|----------|-------|------|
| `GET`  | `/api/v1/organizations/{orgId}/curricula` | Danh sách chương trình | `ROLE_ADMIN`, `ROLE_TEACHER` |
| `POST` | `/api/v1/organizations/{orgId}/curricula` | Tạo chương trình | `ROLE_ADMIN` |
| `GET`  | `/api/v1/organizations/{orgId}/curricula/{curriculumId}` | Chi tiết chương trình | `ROLE_ADMIN`, `ROLE_TEACHER` |
| `POST` | `/api/v1/organizations/{orgId}/curricula/{curriculumId}/subjects` | Thêm môn vào chương trình | `ROLE_ADMIN` |
| `DELETE` | `/api/v1/organizations/{orgId}/curricula/{curriculumId}/subjects/{subjectId}` | Gỡ môn khỏi chương trình | `ROLE_ADMIN` |

**Request Body — Tạo môn học**:
```json
{
  "code": "MATH101",
  "name": "Toán Đại Cương",
  "description": "Môn toán học cơ bản cho sinh viên năm 1",
  "credits": 3
}
```

**Request Body — Thêm môn vào chương trình**:
```json
{
  "subjectId": "550e8400-e29b-41d4-a716-446655440010",
  "isRequired": true,
  "sequenceOrder": 1
}
```

---

## 6. Quy Tắc Nghiệp Vụ

- **Môn học bắt buộc**: `is_required = true` — học sinh phải hoàn thành để lấy chứng chỉ
- **Môn học tự chọn**: `is_required = false` — học sinh chọn đủ tín chỉ từ nhóm tự chọn
- **Tổng tín chỉ**: `total_credits` của chương trình phải ≥ tổng tín chỉ các môn bắt buộc
- **Deprecated subject**: Môn học đã deprecated không thể thêm vào enrollment mới, nhưng các enrollment cũ vẫn hợp lệ

---

## 7. Acceptance Criteria

| ID | Tiêu chí | Loại | Trạng thái |
|----|----------|------|------------|
| AC-5.3.1 | Môn học với `code` trùng trong cùng org → `409 Conflict` | Functional | 📋 Kế hoạch |
| AC-5.3.2 | `credits <= 0` → `400 Bad Request` | Validation | 📋 Kế hoạch |
| AC-5.3.3 | Thêm môn DEPRECATED vào chương trình → `422 Unprocessable Entity` | Validation | 📋 Kế hoạch |
| AC-5.3.4 | Thêm môn đã tồn tại trong chương trình → `409 Conflict` | Functional | 📋 Kế hoạch |
| AC-5.3.5 | `GET /curricula/{id}` trả về danh sách môn học kèm `is_required`, `sequence_order` | Functional | 📋 Kế hoạch |
| AC-5.3.6 | Xóa `Curriculum` → cascade xóa `CurriculumSubject` liên quan | Functional | 📋 Kế hoạch |
| AC-5.3.7 | Flyway migration tạo đúng schema `curriculum` và các bảng | Infrastructure | 📋 Kế hoạch |

---

## 8. Liên Kết

- ← **[Phase 5.2 — Academic Periods & Classes](phase5.2.md)**
- → **[Phase 5.4 — Enrollments & Learning Groups](phase5.4.md)**: `Subject` được tham chiếu trong `EnrollmentEntity`
- → **[Phase 6.5 — Certification Rules](../phase-6/phase6.5.md)**: `Curriculum` được dùng để xác nhận điều kiện hoàn thành
