# Phase 3.4 — Centralized Logging & Correlation ID (`TraceIdFilter`, MDC)

Tài liệu thiết kế kiến trúc, đặc tả kỹ thuật và tiêu chí nghiệm thu cho cơ chế **Logging tập trung** và **Correlation ID** giúp truy vết mọi request xuyên suốt vòng đời xử lý trong hệ thống.

---

## 1. Mục tiêu & Nguyên lý

- **Truy vết Request (Request Tracing)**: Mỗi HTTP request nhận một định danh duy nhất (`traceId`) được gắn vào toàn bộ log entries phát sinh trong quá trình xử lý — giúp kỹ sư có thể tìm kiếm và lọc log theo từng request cụ thể.
- **Correlation ID**: Hỗ trợ giao tiếp với frontend hoặc các hệ thống bên ngoài: client có thể tự gửi `X-Trace-Id` header để liên kết log hai phía; nếu không có, server tự sinh UUID mới.
- **MDC (Mapped Diagnostic Context)**: SLF4J MDC lưu `traceId` theo thread, cho phép pattern logging tự động nhúng `traceId` vào mỗi dòng log mà không cần truyền thủ công qua method parameters.
- **Zero Memory Leak**: `MDC.clear()` bắt buộc được gọi trong `finally` block sau mỗi request để tránh rò rỉ context giữa các request được tái sử dụng bởi thread pool.

---

## 2. Tiêu chí Nghiệm thu (Acceptance Criteria)

| Mã AC | Tiêu chí | Chi tiết kỹ thuật |
| :---: | :--- | :--- |
| **AC-3.4.1** | **TraceId tự sinh nếu thiếu header** | Nếu request không có header `X-Trace-Id`, Filter tự sinh UUID v4 mới và đặt vào MDC. |
| **AC-3.4.2** | **TraceId từ header client** | Nếu request có header `X-Trace-Id`, Filter sử dụng giá trị đó (sau khi sanitize) và đặt vào MDC. |
| **AC-3.4.3** | **TraceId trong Response Header** | Filter luôn trả `X-Trace-Id` trong response header với giá trị traceId đang dùng. |
| **AC-3.4.4** | **MDC.clear() bắt buộc** | Block `finally` trong Filter đảm bảo `MDC.clear()` luôn được gọi, kể cả khi xảy ra exception. |
| **AC-3.4.5** | **Log Pattern nhúng traceId** | Pattern logging trong `logback-spring.xml` bao gồm `%X{traceId}` để mỗi dòng log hiển thị traceId. |
| **AC-3.4.6** | **Đăng ký Filter đúng thứ tự** | `TraceIdFilter` chạy trước `BearerTokenAuthenticationFilter` (thứ tự ưu tiên cao nhất) để đảm bảo traceId có mặt trong mọi log kể cả log security error. |

---

## 3. Triển khai `TraceIdFilter`

```java
// com.edu.boot.web.TraceIdFilter
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter extends OncePerRequestFilter {

    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String MDC_TRACE_KEY = "traceId";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String traceId = resolveTraceId(request);

        try {
            MDC.put(MDC_TRACE_KEY, traceId);
            response.setHeader(TRACE_ID_HEADER, traceId);
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_TRACE_KEY);
        }
    }

    private String resolveTraceId(HttpServletRequest request) {
        String headerTraceId = request.getHeader(TRACE_ID_HEADER);
        if (headerTraceId != null && !headerTraceId.isBlank() && isValidUuid(headerTraceId)) {
            return headerTraceId.trim();
        }
        return UUID.randomUUID().toString();
    }

    private boolean isValidUuid(String value) {
        try {
            UUID.fromString(value.trim());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
```

---

## 4. Cấu hình Logging Pattern (`logback-spring.xml`)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>

    <springProfile name="local,test">
        <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
            <encoder>
                <pattern>
                    %d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [traceId=%X{traceId:-N/A}] %logger{36} - %msg%n
                </pattern>
            </encoder>
        </appender>

        <root level="INFO">
            <appender-ref ref="CONSOLE"/>
        </root>
    </springProfile>

    <springProfile name="prod">
        <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
            <encoder class="net.logstash.logback.encoder.LogstashEncoder">
                <!-- Tự động nhúng MDC fields (traceId) vào JSON log -->
                <includeMdcKeyName>traceId</includeMdcKeyName>
            </encoder>
        </appender>

        <root level="WARN">
            <appender-ref ref="CONSOLE"/>
        </root>
    </springProfile>

</configuration>
```

---

## 5. Ví dụ Log Output

### Môi trường local (plaintext)
```
2026-03-30 10:15:23.456 [http-nio-8080-exec-3] INFO  [traceId=a1b2c3d4-e5f6-7890-abcd-ef1234567890] c.e.identity.web.AccountController - Processing register account request for username: teacher.nguyen
2026-03-30 10:15:23.460 [http-nio-8080-exec-3] INFO  [traceId=a1b2c3d4-e5f6-7890-abcd-ef1234567890] c.e.identity.service.IdentityService - Account registered successfully, userId: e9b2c3a4-1234-4567-89ab-cdef01234567
```

### Môi trường production (JSON via Logstash encoder)
```json
{
  "timestamp": "2026-03-30T10:15:23.456Z",
  "level": "INFO",
  "thread": "http-nio-8080-exec-3",
  "logger": "c.e.identity.web.AccountController",
  "message": "Processing register account request",
  "traceId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
}
```

---

## 6. Tích hợp với ApiErrorResponse

Khi `GlobalExceptionHandler` trả về lỗi, `traceId` có thể được nhúng vào response để client có thể báo cáo chính xác ID khi yêu cầu hỗ trợ kỹ thuật:

```java
// Bổ sung traceId trong ApiErrorResponse
public record ApiErrorResponse(
    Instant timestamp,
    int status,
    String code,
    String message,
    String path,
    List<FieldError> fieldErrors,
    String traceId  // Thêm field này để client biết traceId
) {}

// Trong GlobalExceptionHandler
private ApiErrorResponse buildErrorResponse(HttpStatus status, String code,
        String message, HttpServletRequest request) {
    String traceId = MDC.get("traceId");
    return new ApiErrorResponse(
            Instant.now(), status.value(), code, message,
            request.getRequestURI(), null, traceId
    );
}
```

---

## 7. Liên kết Tài liệu Liên quan

- [Phase 3 README](README.md) — Tổng quan phân hệ Foundation & Observability
- [Phase 3.1: Global Exception Handling](phase3.1.md) — Tích hợp traceId trong ApiErrorResponse
- [Phase 3.5: Actuator & Metrics](phase3.5.md) — Giám sát và health checks
