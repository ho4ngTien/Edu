# Phase 6.4 — Rule Engine Integration (module `rule-engine`)

Tài liệu thiết kế kiến trúc, đặc tả kỹ thuật và tiêu chí nghiệm thu cho phân hệ **Rule Engine** — mô-đun cho phép định nghĩa và đánh giá các quy tắc nghiệp vụ động (dynamic business rules) mà không cần thay đổi mã nguồn cốt lõi.

---

## 1. Mục tiêu & Nguyên lý

- **Tách biệt quy tắc nghiệp vụ khỏi logic ứng dụng**: Các quy tắc như "học sinh đạt khi điểm >= 5.0 VÀ chuyên cần >= 80%" được đóng gói thành các `Rule` object độc lập, không nhúng cứng vào `AssessmentService` hay `AttendanceService`.
- **Khả năng mở rộng (Extensibility)**: Thêm quy tắc mới không yêu cầu sửa đổi các service hiện có — tuân theo **Open/Closed Principle**.
- **Tái sử dụng qua Public Port**: Module `assessment` và `attendance` không gọi trực tiếp vào `rule-engine` implementation, mà thông qua interface `RuleEngine` được khai báo trong package public.
- **Kết quả đánh giá rõ ràng**: Mỗi lần đánh giá trả về `RuleEvaluationResult` với trạng thái PASSED/FAILED và danh sách quy tắc nào vi phạm, giúp dễ debug và giải thích cho người dùng.

---

## 2. Tiêu chí Nghiệm thu (Acceptance Criteria)

| Mã AC | Tiêu chí | Chi tiết kỹ thuật |
| :---: | :--- | :--- |
| **AC-6.4.1** | **Rule Interface & Registry** | Interface `Rule<C>` với phương thức `evaluate(C context): RuleOutcome`. `RuleRegistry` quản lý và tra cứu các rule theo loại. |
| **AC-6.4.2** | **Composite Rule Engine** | `CompositeRuleEngine` nhận danh sách `Rule` và đánh giá tuần tự, trả `RuleEvaluationResult` tổng hợp. |
| **AC-6.4.3** | **Tích hợp qua Public Port** | Module `certification` gọi rule engine qua interface `StudentCompletionRuleEngine` — không import trực tiếp implementation class. |
| **AC-6.4.4** | **Unit Test Cách ly** | Mỗi `Rule` implementation có unit test riêng. `CompositeRuleEngine` được test bằng mock rules. |
| **AC-6.4.5** | **Cấu hình Rule động** | Threshold (điểm tối thiểu, tỉ lệ chuyên cần tối thiểu) có thể cấu hình qua `application.yml` thay vì hardcode. |

---

## 3. Thiết kế Core Interfaces

```java
// com.edu.ruleengine.api.Rule
public interface Rule<C> {
    String getRuleId();
    String getDescription();
    RuleOutcome evaluate(C context);
}

// com.edu.ruleengine.api.RuleOutcome
public record RuleOutcome(
    String ruleId,
    boolean passed,
    String reason
) {
    public static RuleOutcome pass(String ruleId) {
        return new RuleOutcome(ruleId, true, null);
    }

    public static RuleOutcome fail(String ruleId, String reason) {
        return new RuleOutcome(ruleId, false, reason);
    }
}

// com.edu.ruleengine.api.RuleEvaluationResult
public record RuleEvaluationResult(
    boolean overallPassed,
    List<RuleOutcome> outcomes
) {
    public List<RuleOutcome> failedRules() {
        return outcomes.stream().filter(o -> !o.passed()).toList();
    }
}
```

---

## 4. Triển khai Quy tắc Nghiệp vụ

### Context Object (dữ liệu đầu vào cho rule evaluation)

```java
// com.edu.ruleengine.domain.StudentCompletionContext
public record StudentCompletionContext(
    UUID studentId,
    UUID subjectId,
    UUID organizationId,
    double finalGrade,           // Điểm tổng kết (0.0 - 10.0)
    double attendancePercentage, // Tỉ lệ chuyên cần (0.0 - 100.0)
    int totalSessions,
    int attendedSessions
) {}
```

### Rule Implementation

```java
// com.edu.ruleengine.domain.rule.MinimumGradeRule
@Component
public class MinimumGradeRule implements Rule<StudentCompletionContext> {

    private static final String RULE_ID = "RULE_MIN_GRADE";

    @Value("${edu.rules.completion.minimum-grade:5.0}")
    private double minimumGrade;

    @Override
    public String getRuleId() { return RULE_ID; }

    @Override
    public String getDescription() {
        return "Điểm tổng kết phải đạt tối thiểu " + minimumGrade + "/10";
    }

    @Override
    public RuleOutcome evaluate(StudentCompletionContext context) {
        if (context.finalGrade() >= minimumGrade) {
            return RuleOutcome.pass(RULE_ID);
        }
        return RuleOutcome.fail(RULE_ID,
                String.format("Điểm tổng kết %.1f thấp hơn mức yêu cầu %.1f",
                        context.finalGrade(), minimumGrade));
    }
}

// com.edu.ruleengine.domain.rule.MinimumAttendanceRule
@Component
public class MinimumAttendanceRule implements Rule<StudentCompletionContext> {

    private static final String RULE_ID = "RULE_MIN_ATTENDANCE";

    @Value("${edu.rules.completion.minimum-attendance-percent:80.0}")
    private double minimumAttendancePercent;

    @Override
    public String getRuleId() { return RULE_ID; }

    @Override
    public String getDescription() {
        return "Tỉ lệ chuyên cần phải đạt tối thiểu " + minimumAttendancePercent + "%";
    }

    @Override
    public RuleOutcome evaluate(StudentCompletionContext context) {
        if (context.attendancePercentage() >= minimumAttendancePercent) {
            return RuleOutcome.pass(RULE_ID);
        }
        return RuleOutcome.fail(RULE_ID,
                String.format("Chuyên cần %.1f%% thấp hơn mức yêu cầu %.1f%%",
                        context.attendancePercentage(), minimumAttendancePercent));
    }
}
```

### Composite Engine

```java
// com.edu.ruleengine.application.CompositeRuleEngine
@Service
public class CompositeRuleEngine {

    private final List<Rule<StudentCompletionContext>> completionRules;

    public CompositeRuleEngine(List<Rule<StudentCompletionContext>> completionRules) {
        this.completionRules = completionRules;
    }

    public RuleEvaluationResult evaluate(StudentCompletionContext context) {
        List<RuleOutcome> outcomes = completionRules.stream()
                .map(rule -> rule.evaluate(context))
                .toList();

        boolean allPassed = outcomes.stream().allMatch(RuleOutcome::passed);
        return new RuleEvaluationResult(allPassed, outcomes);
    }
}
```

---

## 5. Cấu hình Rule Thresholds (`application.yml`)

```yaml
edu:
  rules:
    completion:
      minimum-grade: 5.0
      minimum-attendance-percent: 80.0
```

---

## 6. Liên kết Tài liệu Liên quan

- [Phase 6 README](README.md) — Tổng quan phân hệ Academic Operations
- [Phase 6.3: Assessments & Grades](phase6.3.md) — Cung cấp `finalGrade` cho rule context
- [Phase 6.2: Attendance](phase6.2.md) — Cung cấp `attendancePercentage` cho rule context
- [Phase 6.5: Completion & Certification](phase6.5.md) — Consumer của Rule Engine

