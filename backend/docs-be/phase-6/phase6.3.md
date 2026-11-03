# Phase 6.3 — Assessments & Grades (Module `assessment`)

> **Module**: `assessment` | **Package**: `com.edu.assessment` | **Schema DB**: `assessment`

---

## 1. Mục Tiêu

Module `assessment` quản lý **bài kiểm tra** (`Assessment`) và **điểm số** (`Grade`) của học sinh. Hỗ trợ tính điểm tổng kết theo trọng số và kiểm soát quyền nhập điểm ở mức resource (chỉ giáo viên phụ trách mới được nhập điểm).

**Phạm vi**:
- Định nghĩa các cột điểm (MIDTERM, FINAL, QUIZ, ASSIGNMENT)
- Ghi nhận và cập nhật điểm số từng học sinh
- Tính điểm tổng kết theo trọng số (`WeightedGradeCalculator`)
- Kiểm soát quyền nhập điểm (resource-level authorization)
- Cung cấp public port để `rule-engine` và `certification` truy vấn kết quả

---

## 2. Kiến Trúc Module

```
com.edu.assessment/
├── domain/
│   ├── Assessment.java
│   ├── Grade.java
│   ├── AssessmentType.java           # Enum: MIDTERM, FINAL, QUIZ, ASSIGNMENT, PROJECT
│   ├── GradeStatus.java              # Enum: DRAFT, PUBLISHED, LOCKED
│   └── WeightedGradeCalculator.java  # Domain service: tính điểm tổng kết
├── application/
│   ├── port/
│   │   ├── in/
│   │   │   ├── CreateAssessmentUseCase.java
│   │   │   ├── SubmitGradeUseCase.java
│   │   │   ├── PublishGradesUseCase.java
│   │   │   └── GetFinalGradeUseCase.java    # Public port
│   │   └── out/
│   │       ├── AssessmentRepository.java
│   │       ├── GradeRepository.java
│   │       └── TeacherAssignmentPort.java   # Port để verify giáo viên phụ trách
│   └── service/
│       ├── AssessmentService.java
│       └── GradeService.java
├── adapter/
│   ├── in/
│   │   └── web/
│   │       ├── AssessmentController.java
│   │       └── GradeController.java
│   └── out/
│       └── persistence/
│           ├── AssessmentEntity.java
│           ├── GradeEntity.java
│           ├── AssessmentJpaRepository.java
│           └── GradeJpaRepository.java
└── AssessmentModule.java
```

---

## 3. Thiết Kế Entity

### 3.1 `AssessmentEntity`

```java
@Entity
@Table(schema = "assessment", name = "assessments")
public class AssessmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;               // Tên cột điểm: "Giữa kỳ", "Cuối kỳ", "Quiz 1"

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private AssessmentType type;       // MIDTERM, FINAL, QUIZ, ASSIGNMENT, PROJECT

    @Column(name = "weight", nullable = false)
    private BigDecimal weight;         // Trọng số (0.0 - 1.0). Tổng tất cả = 1.0

    @Column(name = "max_score", nullable = false)
    private BigDecimal maxScore;       // Điểm tối đa (thường 10.0)

    @Column(name = "subject_id", nullable = false)
    private UUID subjectId;            // Logical ref → curriculum.subjects

    @Column(name = "semester_id", nullable = false)
    private UUID semesterId;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private GradeStatus status;        // DRAFT, PUBLISHED, LOCKED

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
```

### 3.2 `GradeEntity`

```java
@Entity
@Table(schema = "assessment", name = "grades",
       uniqueConstraints = @UniqueConstraint(
           columnNames = {"student_id", "assessment_id"}
       ))
public class GradeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "assessment_id", nullable = false)
    private UUID assessmentId;

    @Column(name = "score")
    private BigDecimal score;          // Điểm số (null nếu chưa nhập)

    @Column(name = "comment")
    private String comment;

    @Column(name = "graded_by")
    private UUID gradedBy;             // Teacher ID

    @Column(name = "graded_at")
    private Instant gradedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
```

### 3.3 `WeightedGradeCalculator` (Domain Service)

```java
public class WeightedGradeCalculator {

    /**
     * Tính điểm tổng kết theo trọng số.
     * @param grades Map<Assessment, Grade> — cặp đánh giá và điểm số
     * @return điểm tổng kết (0.0 - 10.0), hoặc empty nếu thiếu điểm bắt buộc
     */
    public Optional<BigDecimal> calculate(Map<Assessment, Grade> grades) {
        BigDecimal totalWeight = BigDecimal.ZERO;
        BigDecimal weightedSum = BigDecimal.ZERO;

        for (Map.Entry<Assessment, Grade> entry : grades.entrySet()) {
            Assessment assessment = entry.getKey();
            Grade grade = entry.getValue();

            if (grade.getScore() == null) {
                if (assessment.getType() == FINAL || assessment.getType() == MIDTERM) {
                    return Optional.empty(); // Bắt buộc phải có điểm
                }
                continue; // Bỏ qua quiz/assignment chưa có điểm
            }

            BigDecimal normalizedScore = grade.getScore()
                .divide(assessment.getMaxScore(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.TEN); // Quy về thang 10

            weightedSum = weightedSum.add(normalizedScore.multiply(assessment.getWeight()));
            totalWeight = totalWeight.add(assessment.getWeight());
        }

        if (totalWeight.compareTo(BigDecimal.ZERO) == 0) return Optional.empty();

        return Optional.of(
            weightedSum.divide(totalWeight, 2, RoundingMode.HALF_UP)
        );
    }
}
```

---

## 4. Flyway Migrations

### `V29__create_assessment_schema.sql`
```sql
CREATE SCHEMA IF NOT EXISTS assessment;
```

### `V30__create_assessment_tables.sql`
```sql
CREATE TABLE assessment.assessments (
    id              UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(100)   NOT NULL,
    type            VARCHAR(20)    NOT NULL,
    weight          NUMERIC(4,3)   NOT NULL CHECK (weight > 0 AND weight <= 1),
    max_score       NUMERIC(5,2)   NOT NULL DEFAULT 10.00,
    subject_id      UUID           NOT NULL,
    semester_id     UUID           NOT NULL,
    organization_id UUID           NOT NULL,
    status          VARCHAR(20)    NOT NULL DEFAULT 'DRAFT',
    created_at      TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ    NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_assessment_type   CHECK (type IN ('MIDTERM','FINAL','QUIZ','ASSIGNMENT','PROJECT')),
    CONSTRAINT chk_assessment_status CHECK (status IN ('DRAFT','PUBLISHED','LOCKED'))
);

CREATE TABLE assessment.grades (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id      UUID         NOT NULL,
    assessment_id   UUID         NOT NULL REFERENCES assessment.assessments(id) ON DELETE RESTRICT,
    score           NUMERIC(5,2) CHECK (score >= 0),
    comment         TEXT,
    graded_by       UUID,
    graded_at       TIMESTAMPTZ,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_grade_student_assessment UNIQUE (student_id, assessment_id)
);

CREATE INDEX idx_grades_student    ON assessment.grades(student_id);
CREATE INDEX idx_grades_assessment ON assessment.grades(assessment_id);
CREATE INDEX idx_assessments_subject_semester
    ON assessment.assessments(subject_id, semester_id);
```

---

## 5. REST API & Resource-Level Authorization

### 5.1 Assessment Endpoints

| Method | Endpoint | Mô tả | Auth |
|--------|----------|-------|------|
| `GET`  | `/api/v1/organizations/{orgId}/assessments` | Danh sách cột điểm | `ROLE_ADMIN`, `ROLE_TEACHER` |
| `POST` | `/api/v1/organizations/{orgId}/assessments` | Tạo cột điểm | `ROLE_ADMIN` |
| `PUT`  | `/api/v1/organizations/{orgId}/assessments/{id}` | Cập nhật cột điểm | `ROLE_ADMIN` |
| `POST` | `/api/v1/organizations/{orgId}/assessments/{id}/publish` | Publish điểm | `ROLE_ADMIN` |

### 5.2 Grade Endpoints

| Method | Endpoint | Mô tả | Auth |
|--------|----------|-------|------|
| `POST` | `/api/v1/organizations/{orgId}/grades/batch` | Nhập điểm hàng loạt | `ROLE_TEACHER` (chỉ môn phụ trách) |
| `GET`  | `/api/v1/organizations/{orgId}/students/{studentId}/grades` | Xem điểm học sinh | `ROLE_ADMIN`, `ROLE_TEACHER` |
| `GET`  | `/api/v1/organizations/{orgId}/subjects/{subjectId}/final-grades` | Điểm tổng kết môn | `ROLE_ADMIN`, `ROLE_TEACHER` |

> [!IMPORTANT]
> **Resource-Level Authorization**: `GradeService.submitGrade()` kiểm tra `teacherId` của người đang đăng nhập có nằm trong `TeachingAssignment` cho `subjectId` và `semesterId` tương ứng không. Nếu không → `403 Forbidden`.

```java
// GradeService.java
public void submitGrade(SubmitGradeCommand command, UUID currentTeacherId) {
    boolean isAssigned = teacherAssignmentPort.isTeacherAssigned(
        currentTeacherId, command.subjectId(), command.semesterId()
    );
    if (!isAssigned) {
        throw new AccessDeniedException("Giáo viên không được phân công môn này");
    }
    // ...
}
```

---

## 6. Acceptance Criteria

| ID | Tiêu chí | Loại | Trạng thái |
|----|----------|------|------------|
| AC-6.3.1 | Giáo viên không phụ trách nhập điểm → `403 Forbidden` | Security | 📋 Kế hoạch |
| AC-6.3.2 | Tổng `weight` của các assessment trong môn ≠ 1.0 → cảnh báo khi tính điểm | Validation | 📋 Kế hoạch |
| AC-6.3.3 | `score > maxScore` → `400 Bad Request` | Validation | 📋 Kế hoạch |
| AC-6.3.4 | Điểm tổng kết được tính đúng theo công thức trọng số | Functional | 📋 Kế hoạch |
| AC-6.3.5 | Assessment có `status = LOCKED` không cho phép sửa điểm | Functional | 📋 Kế hoạch |
| AC-6.3.6 | `GET /final-grades` trả về `finalScore` và `isPassed` (>= 5.0) | Functional | 📋 Kế hoạch |
| AC-6.3.7 | Flyway V29–V30 chạy thành công | Infrastructure | 📋 Kế hoạch |

---

## 7. Liên Kết

- ← **[Phase 6.1 — Teaching Assignments](phase6.1.md)**: Kiểm tra giáo viên phụ trách
- → **[Phase 6.4 — Rule Engine](phase6.4.md)**: `GetFinalGradeUseCase` port được dùng bởi rule-engine
- → **[Phase 6.5 — Certification](phase6.5.md)**: Điểm tổng kết là input để xét hoàn thành
