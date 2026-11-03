# Phase 6.2 — Attendance Recording & Tracking (Module `attendance`)

> **Module**: `attendance` | **Package**: `com.edu.attendance` | **Schema DB**: `attendance`

---

## 1. Mục Tiêu

Module `attendance` xử lý **điểm danh học sinh** theo từng buổi học và tổng kết **tỉ lệ chuyên cần** theo học kỳ. Dữ liệu điểm danh được sử dụng bởi `rule-engine` để đánh giá điều kiện đạt môn.

**Phạm vi**:
- Ghi nhận trạng thái điểm danh của từng học sinh trong mỗi buổi học
- Tính toán và cập nhật tóm tắt chuyên cần (`AttendanceSummary`) theo kỳ
- Cho phép giáo viên sửa điểm danh (với audit log)
- Cung cấp public port để `rule-engine` truy vấn tỉ lệ chuyên cần

---

## 2. Kiến Trúc Module

```
com.edu.attendance/
├── domain/
│   ├── AttendanceRecord.java
│   ├── AttendanceSummary.java
│   └── AttendanceStatus.java         # Enum: PRESENT, ABSENT, LATE, EXCUSED
├── application/
│   ├── port/
│   │   ├── in/
│   │   │   ├── RecordAttendanceUseCase.java
│   │   │   ├── UpdateAttendanceUseCase.java
│   │   │   └── GetAttendanceSummaryUseCase.java
│   │   └── out/
│   │       ├── AttendanceRecordRepository.java
│   │       ├── AttendanceSummaryRepository.java
│   │       └── AttendanceQueryPort.java       # Public port cho rule-engine
│   └── service/
│       ├── AttendanceService.java
│       └── AttendanceSummaryService.java
├── adapter/
│   ├── in/
│   │   └── web/
│   │       └── AttendanceController.java
│   └── out/
│       └── persistence/
│           ├── AttendanceRecordEntity.java
│           ├── AttendanceSummaryEntity.java
│           ├── AttendanceRecordJpaRepository.java
│           └── AttendanceSummaryJpaRepository.java
└── AttendanceModule.java
```

---

## 3. Thiết Kế Entity

### 3.1 `AttendanceRecordEntity`

```java
@Entity
@Table(schema = "attendance", name = "attendance_records")
public class AttendanceRecordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;            // Logical ref → people.students

    @Column(name = "schedule_id", nullable = false)
    private UUID scheduleId;           // Logical ref → teaching.schedules

    @Column(name = "subject_id", nullable = false)
    private UUID subjectId;            // Denormalized để query nhanh

    @Column(name = "semester_id", nullable = false)
    private UUID semesterId;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "session_date", nullable = false)
    private LocalDate sessionDate;     // Ngày buổi học

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AttendanceStatus status;   // PRESENT, ABSENT, LATE, EXCUSED

    @Column(name = "note")
    private String note;               // Ghi chú của giáo viên

    @Column(name = "recorded_by")
    private UUID recordedBy;           // Teacher ID đã điểm danh

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
```

### 3.2 `AttendanceSummaryEntity`

Tóm tắt chuyên cần được tính toán và cập nhật sau mỗi lần điểm danh:

```java
@Entity
@Table(schema = "attendance", name = "attendance_summaries",
       uniqueConstraints = @UniqueConstraint(
           columnNames = {"student_id", "subject_id", "semester_id"}
       ))
public class AttendanceSummaryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "subject_id", nullable = false)
    private UUID subjectId;

    @Column(name = "semester_id", nullable = false)
    private UUID semesterId;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "total_sessions", nullable = false)
    private Integer totalSessions;      // Tổng số buổi học

    @Column(name = "present_count", nullable = false)
    private Integer presentCount;       // Số buổi có mặt

    @Column(name = "absent_count", nullable = false)
    private Integer absentCount;

    @Column(name = "late_count", nullable = false)
    private Integer lateCount;

    @Column(name = "excused_count", nullable = false)
    private Integer excusedCount;

    @Column(name = "attendance_rate", nullable = false)
    private BigDecimal attendanceRate;  // % chuyên cần = (present + excused) / total * 100

    @Column(name = "last_calculated_at", nullable = false)
    private Instant lastCalculatedAt;
}
```

---

## 4. Flyway Migrations

### `V26__create_attendance_schema.sql`
```sql
CREATE SCHEMA IF NOT EXISTS attendance;
```

### `V27__create_attendance_records.sql`
```sql
CREATE TABLE attendance.attendance_records (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id      UUID        NOT NULL,
    schedule_id     UUID        NOT NULL,
    subject_id      UUID        NOT NULL,
    semester_id     UUID        NOT NULL,
    organization_id UUID        NOT NULL,
    session_date    DATE        NOT NULL,
    status          VARCHAR(10) NOT NULL,
    note            TEXT,
    recorded_by     UUID,
    recorded_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_attendance_status
        CHECK (status IN ('PRESENT','ABSENT','LATE','EXCUSED'))
);

CREATE UNIQUE INDEX uq_attendance_student_schedule
    ON attendance.attendance_records(student_id, schedule_id);

CREATE INDEX idx_attendance_student_semester
    ON attendance.attendance_records(student_id, semester_id);
CREATE INDEX idx_attendance_session_date
    ON attendance.attendance_records(session_date);
```

### `V28__create_attendance_summaries.sql`
```sql
CREATE TABLE attendance.attendance_summaries (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id        UUID           NOT NULL,
    subject_id        UUID           NOT NULL,
    semester_id       UUID           NOT NULL,
    organization_id   UUID           NOT NULL,
    total_sessions    INTEGER        NOT NULL DEFAULT 0,
    present_count     INTEGER        NOT NULL DEFAULT 0,
    absent_count      INTEGER        NOT NULL DEFAULT 0,
    late_count        INTEGER        NOT NULL DEFAULT 0,
    excused_count     INTEGER        NOT NULL DEFAULT 0,
    attendance_rate   NUMERIC(5,2)   NOT NULL DEFAULT 0.00,
    last_calculated_at TIMESTAMPTZ   NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_summary_student_subject_semester
        UNIQUE (student_id, subject_id, semester_id),
    CONSTRAINT chk_attendance_rate
        CHECK (attendance_rate BETWEEN 0 AND 100)
);
```

---

## 5. REST API

| Method | Endpoint | Mô tả | Auth |
|--------|----------|-------|------|
| `POST` | `/api/v1/organizations/{orgId}/attendance` | Ghi nhận điểm danh (batch) | `ROLE_TEACHER` |
| `PUT`  | `/api/v1/organizations/{orgId}/attendance/{recordId}` | Sửa điểm danh | `ROLE_TEACHER`, `ROLE_ADMIN` |
| `GET`  | `/api/v1/organizations/{orgId}/attendance` | Danh sách điểm danh (filter) | `ROLE_ADMIN`, `ROLE_TEACHER` |
| `GET`  | `/api/v1/organizations/{orgId}/students/{studentId}/attendance-summary` | Tóm tắt chuyên cần của học sinh | `ROLE_ADMIN`, `ROLE_TEACHER` |
| `GET`  | `/api/v1/organizations/{orgId}/subjects/{subjectId}/attendance-summary` | Tóm tắt chuyên cần theo môn | `ROLE_ADMIN`, `ROLE_TEACHER` |

**Request Body — Điểm danh batch**:
```json
{
  "scheduleId": "550e8400-e29b-41d4-a716-446655440050",
  "sessionDate": "2024-10-07",
  "records": [
    { "studentId": "...", "status": "PRESENT" },
    { "studentId": "...", "status": "ABSENT", "note": "Không phép" },
    { "studentId": "...", "status": "LATE" }
  ]
}
```

---

## 6. Tính Toán Tỉ Lệ Chuyên Cần

```java
// AttendanceSummaryService.java
public void recalculate(UUID studentId, UUID subjectId, UUID semesterId) {
    List<AttendanceRecord> records = repository.findByStudentSubjectSemester(
        studentId, subjectId, semesterId
    );

    int total    = records.size();
    int present  = (int) records.stream().filter(r -> r.getStatus() == PRESENT).count();
    int excused  = (int) records.stream().filter(r -> r.getStatus() == EXCUSED).count();
    int absent   = (int) records.stream().filter(r -> r.getStatus() == ABSENT).count();
    int late     = (int) records.stream().filter(r -> r.getStatus() == LATE).count();

    BigDecimal rate = total == 0 ? BigDecimal.ZERO
        : BigDecimal.valueOf((present + excused) * 100.0 / total)
                    .setScale(2, RoundingMode.HALF_UP);

    // Upsert summary
    summaryRepository.upsert(studentId, subjectId, semesterId,
        total, present, absent, late, excused, rate);
}
```

> [!NOTE]
> `LATE` được tính là **có mặt nhưng đến muộn** — không cộng vào `present_count` nhưng được cộng vào `attendance_rate` với hệ số 0.5 (cấu hình linh hoạt qua `rule-engine`).

---

## 7. Acceptance Criteria

| ID | Tiêu chí | Loại | Trạng thái |
|----|----------|------|------------|
| AC-6.2.1 | Điểm danh batch → tự động tính lại `attendance_rate` cho từng học sinh | Functional | 📋 Kế hoạch |
| AC-6.2.2 | Chỉ giáo viên được phân công mới điểm danh được lớp đó | Security | 📋 Kế hoạch |
| AC-6.2.3 | `GET /students/{id}/attendance-summary` trả về tỉ lệ chuyên cần đúng | Functional | 📋 Kế hoạch |
| AC-6.2.4 | Sửa điểm danh sau khi đã ghi → cập nhật lại `attendance_rate` | Functional | 📋 Kế hoạch |
| AC-6.2.5 | `attendance_rate` không vượt quá 100.00 | Validation | 📋 Kế hoạch |
| AC-6.2.6 | Flyway V26–V28 chạy thành công | Infrastructure | 📋 Kế hoạch |

---

## 8. Liên Kết

- ← **[Phase 6.1 — Teaching Schedules](phase6.1.md)**: `schedule_id` dùng để gắn buổi học
- → **[Phase 6.4 — Rule Engine](phase6.4.md)**: `AttendanceQueryPort` được dùng bởi rule-engine
