# Phase 6.5 — Completion & Certification Rules (module `certification`)

Tài liệu thiết kế kiến trúc, đặc tả kỹ thuật và tiêu chí nghiệm thu cho phân hệ **Chứng nhận Hoàn thành Học phần** — xác định học sinh đủ điều kiện hoàn thành môn học/khóa học và lưu trữ kết quả chứng nhận.

---

## 1. Mục tiêu & Phạm vi

- **Đánh giá điều kiện hoàn thành**: Kết hợp dữ liệu từ `assessment` (điểm số) và `attendance` (chuyên cần) thông qua `rule-engine` để xác định học sinh có đạt yêu cầu hoàn thành môn học.
- **Lưu trữ Chứng nhận**: Phát hành và lưu trữ `CertificationRecord` khi học sinh đạt đủ điều kiện.
- **Domain Event**: Phát sự kiện `StudentCertifiedEvent` để các module khác (ví dụ: notification, transcript) phản ứng mà không cần coupling trực tiếp.
- **Cô lập Module**: Module `certification` giao tiếp với `assessment` và `attendance` qua Public Port, không import entity nội bộ.

---

## 2. Tiêu chí Nghiệm thu (Acceptance Criteria)

| Mã AC | Tiêu chí | Chi tiết kỹ thuật |
| :---: | :--- | :--- |
| **AC-6.5.1** | **Database Schema & Migration** | Flyway migration tạo bảng `certification.certification_records` với fields: `id`, `student_id`, `subject_id`, `organization_id`, `semester_id`, `final_grade`, `attendance_percentage`, `status` (PASSED/FAILED), `evaluated_at`. |
| **AC-6.5.2** | **Evaluation Use Case** | `CertificationService.evaluate()` nhận `EvaluateCertificationCommand`, thu thập dữ liệu từ ports, chạy qua `CompositeRuleEngine`, lưu kết quả, phát `StudentCertifiedEvent`. |
| **AC-6.5.3** | **Domain Event Publication** | `StudentCertifiedEvent` phát qua `ApplicationEventPublisher`. Listener trong module notification (tương lai) subscribe mà không phụ thuộc `certification` implementation. |
| **AC-6.5.4** | **REST API Evaluation Endpoint** | `POST /api/v1/organizations/{orgId}/certifications/evaluate` nhận `{studentId, subjectId, semesterId}` và trả kết quả đánh giá với chi tiết các quy tắc. |
| **AC-6.5.5** | **Idempotency** | Đánh giá lại cùng một học sinh trong cùng học kỳ cho cùng môn học sẽ cập nhật kết quả hiện có, không tạo bản ghi trùng lặp. |

---

## 3. Thiết kế Database Schema

```sql
CREATE SCHEMA IF NOT EXISTS certification;

CREATE TABLE IF NOT EXISTS certification.certification_records (
    id UUID PRIMARY KEY,
    student_id UUID NOT NULL,
    subject_id UUID NOT NULL,
    organization_id UUID NOT NULL,
    semester_id UUID NOT NULL,
    final_grade DECIMAL(4,2),
    attendance_percentage DECIMAL(5,2),
    status VARCHAR(10) NOT NULL,
    failed_rules JSONB,
    evaluated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_cert_student_subject_semester UNIQUE (student_id, subject_id, semester_id),
    CONSTRAINT ck_cert_status CHECK (status IN ('PASSED', 'FAILED', 'PENDING'))
);

CREATE INDEX IF NOT EXISTS idx_cert_org_id ON certification.certification_records (organization_id);
CREATE INDEX IF NOT EXISTS idx_cert_student_id ON certification.certification_records (student_id);
CREATE INDEX IF NOT EXISTS idx_cert_status ON certification.certification_records (status);
```

---

## 4. Domain Event

```java
// com.edu.certification.domain.event.StudentCertifiedEvent
public record StudentCertifiedEvent(
    UUID studentId,
    UUID subjectId,
    UUID organizationId,
    UUID semesterId,
    String status,      // "PASSED" hoặc "FAILED"
    double finalGrade,
    double attendancePercentage,
    Instant evaluatedAt
) implements DomainEvent {

    @Override
    public Instant occurredOn() {
        return evaluatedAt;
    }
}
```

---

## 5. Public Ports (Giao tiếp với module khác)

```java
// Port để lấy điểm tổng kết từ module assessment
// com.edu.certification.application.port.StudentGradeQuery
public interface StudentGradeQuery {
    OptionalDouble getFinalGrade(UUID studentId, UUID subjectId, UUID semesterId);
}

// Port để lấy chuyên cần từ module attendance
// com.edu.certification.application.port.StudentAttendanceQuery
public interface StudentAttendanceQuery {
    double getAttendancePercentage(UUID studentId, UUID subjectId, UUID semesterId);
}
```

---

## 6. Triển khai CertificationService

```java
// com.edu.certification.application.service.CertificationService
@Service
@Transactional
public class CertificationService {

    private final CertificationRecordRepository certificationRepository;
    private final StudentGradeQuery gradeQuery;
    private final StudentAttendanceQuery attendanceQuery;
    private final CompositeRuleEngine ruleEngine;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    public CertificationEvaluationResult evaluate(EvaluateCertificationCommand command) {
        OrganizationId orgId = TenantContext.getOrganizationId();

        // Thu thập dữ liệu qua Public Ports
        double finalGrade = gradeQuery.getFinalGrade(
                command.studentId(), command.subjectId(), command.semesterId())
                .orElseThrow(() -> new BusinessException(ErrorCode.GRADE_NOT_FOUND,
                        "No final grade found for student and subject"));

        double attendancePercent = attendanceQuery.getAttendancePercentage(
                command.studentId(), command.subjectId(), command.semesterId());

        // Đánh giá qua Rule Engine
        StudentCompletionContext context = new StudentCompletionContext(
                command.studentId(), command.subjectId(), orgId.value(),
                finalGrade, attendancePercent, 0, 0
        );
        RuleEvaluationResult result = ruleEngine.evaluate(context);

        // Lưu kết quả (idempotent - update nếu đã tồn tại)
        String status = result.overallPassed() ? "PASSED" : "FAILED";
        Instant now = clock.instant();

        certificationRepository.findByStudentAndSubjectAndSemester(
                command.studentId(), command.subjectId(), command.semesterId())
                .ifPresentOrElse(
                    existing -> existing.updateResult(status, finalGrade, attendancePercent,
                            result.failedRules(), now),
                    () -> certificationRepository.save(new CertificationRecordEntity(
                            UUID.randomUUID(), command.studentId(), command.subjectId(),
                            orgId.value(), command.semesterId(),
                            finalGrade, attendancePercent, status,
                            serializeFailedRules(result.failedRules()), now))
                );

        // Phát Domain Event
        eventPublisher.publishEvent(new StudentCertifiedEvent(
                command.studentId(), command.subjectId(), orgId.value(),
                command.semesterId(), status, finalGrade, attendancePercent, now
        ));

        return new CertificationEvaluationResult(status, finalGrade, attendancePercent,
                result.outcomes());
    }
}
```

---

## 7. REST API

### Đánh giá chứng nhận (`POST /api/v1/organizations/{orgId}/certifications/evaluate`)

**Request Body:**
```json
{
  "studentId": "11112222-3333-4444-5555-666677778888",
  "subjectId": "aaaa1111-bbbb-2222-cccc-3333dddd4444",
  "semesterId": "eeee5555-ffff-6666-0000-1111aaaa2222"
}
```

**Response 200 OK:**
```json
{
  "studentId": "11112222-3333-4444-5555-666677778888",
  "subjectId": "aaaa1111-bbbb-2222-cccc-3333dddd4444",
  "status": "FAILED",
  "finalGrade": 6.5,
  "attendancePercentage": 72.5,
  "evaluatedRules": [
    { "ruleId": "RULE_MIN_GRADE", "passed": true, "reason": null },
    { "ruleId": "RULE_MIN_ATTENDANCE", "passed": false, "reason": "Chuyên cần 72.5% thấp hơn mức yêu cầu 80.0%" }
  ]
}
```

### Xem danh sách chứng nhận (`GET /api/v1/organizations/{orgId}/certifications`)

**Query Params:** `?semesterId=...&status=PASSED&page=0&size=20`

---

## 8. Liên kết Tài liệu Liên quan

- [Phase 6 README](README.md) — Tổng quan phân hệ Academic Operations
- [Phase 6.3: Assessments & Grades](phase6.3.md) — Cung cấp điểm số
- [Phase 6.2: Attendance](phase6.2.md) — Cung cấp tỉ lệ chuyên cần
- [Phase 6.4: Rule Engine](phase6.4.md) — Đánh giá quy tắc hoàn thành

