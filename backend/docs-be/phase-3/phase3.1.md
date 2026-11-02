# Phase 3.1 — Global Exception Handling & Standard API Error Response

> **Dự án:** E-Edu Backend · Java 21 + Spring Boot 3.4 · Modular Monolith  
> **Package gốc:** `com.edu`  
> **Trạng thái:** ✅ Hoàn thành

---

## 1. Mục tiêu

Chuẩn hóa **toàn bộ phản hồi lỗi** của REST API trong hệ thống E-Edu theo một contract duy nhất, nhất quán, dễ tích hợp cho frontend và API consumer.

| Vấn đề cần giải quyết | Giải pháp |
|---|---|
| Lỗi validation trả nhiều format khác nhau | Dùng `ApiErrorResponse` thống nhất |
| Stack trace lộ ra ngoài production | `GlobalExceptionHandler` bắt tất cả, log nội bộ |
| Business logic errors không có mã lỗi rõ ràng | `BusinessException` + `ErrorCode` enum |
| HTTP status code không nhất quán | Map `ErrorCode` → HTTP status cố định |

---

## 2. Kiến trúc tổng quan

```
HTTP Request
     │
     ▼
┌─────────────────────────────────────┐
│           @RestController           │
│  throws BusinessException /         │
│  MethodArgumentNotValidException    │
└────────────────┬────────────────────┘
                 │ (exception propagates)
                 ▼
┌─────────────────────────────────────┐
│      GlobalExceptionHandler         │
│      @RestControllerAdvice          │
│                                     │
│  ┌──────────────────────────────┐   │
│  │  Mapping logic:              │   │
│  │  Exception → ApiErrorResponse│   │
│  └──────────────────────────────┘   │
└────────────────┬────────────────────┘
                 │
                 ▼
         ResponseEntity<ApiErrorResponse>
```

---

## 3. `ApiErrorResponse.java`

**Package:** `com.edu.boot.web`

```java
package com.edu.boot.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;

/**
 * Contract chuẩn cho mọi phản hồi lỗi REST API của hệ thống E-Edu.
 * Được serialize thành JSON và trả về trong body của mọi response lỗi.
 *
 * @param timestamp  Thời điểm xảy ra lỗi (ISO-8601 UTC)
 * @param status     HTTP status code (e.g. 400, 422, 500)
 * @param code       Mã lỗi nghiệp vụ định nghĩa trong {@link com.edu.shared.kernel.exception.ErrorCode}
 * @param message    Mô tả lỗi thân thiện với developer/người dùng
 * @param path       URI request gây ra lỗi
 * @param fieldErrors Danh sách lỗi field (chỉ có khi validation thất bại)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String code,
        String message,
        String path,
        List<FieldError> fieldErrors
) {
    /**
     * Đại diện cho một lỗi validation trên một field cụ thể.
     *
     * @param field   Tên field vi phạm constraint
     * @param message Mô tả vi phạm (từ annotation message hoặc custom message source)
     */
    public record FieldError(String field, String message) {}

    /** Factory method cho lỗi đơn giản (không có fieldErrors). */
    public static ApiErrorResponse of(int status, String code, String message, String path) {
        return new ApiErrorResponse(Instant.now(), status, code, message, path, null);
    }

    /** Factory method cho lỗi validation có danh sách field errors. */
    public static ApiErrorResponse ofValidation(int status, String code, String message,
                                                 String path, List<FieldError> fieldErrors) {
        return new ApiErrorResponse(Instant.now(), status, code, message, path, fieldErrors);
    }
}
```

### Ví dụ JSON response

```json
{
  "timestamp": "2025-11-02T10:30:00.000Z",
  "status": 422,
  "code": "VALIDATION_ERROR",
  "message": "Dữ liệu đầu vào không hợp lệ",
  "path": "/api/v1/users",
  "fieldErrors": [
    { "field": "email", "message": "Email không đúng định dạng" },
    { "field": "username", "message": "Username không được để trống" }
  ]
}
```

---

## 4. `ErrorCode.java`

**Package:** `com.edu.shared.kernel.exception`

```java
package com.edu.shared.kernel.exception;

import org.springframework.http.HttpStatus;

/**
 * Enum tập trung toàn bộ mã lỗi nghiệp vụ của hệ thống E-Edu.
 * Mỗi ErrorCode ánh xạ 1-1 với một HTTP status code cụ thể.
 */
public enum ErrorCode {

    // ─── Validation ────────────────────────────────────────────────────────
    /** Dữ liệu đầu vào không vượt qua Jakarta Bean Validation */
    VALIDATION_ERROR(HttpStatus.UNPROCESSABLE_ENTITY),

    // ─── Identity Domain ───────────────────────────────────────────────────
    /** Không tìm thấy người dùng theo ID hoặc username */
    USER_NOT_FOUND(HttpStatus.NOT_FOUND),

    /** Username đã tồn tại trong hệ thống */
    DUPLICATE_USERNAME(HttpStatus.CONFLICT),

    /** Email đã được đăng ký bởi tài khoản khác */
    DUPLICATE_EMAIL(HttpStatus.CONFLICT),

    /** Thông tin xác thực (username/password) không chính xác */
    BAD_CREDENTIALS(HttpStatus.UNAUTHORIZED),

    // ─── Organization Domain ───────────────────────────────────────────────
    /** Không tìm thấy tổ chức theo ID hoặc code */
    ORG_NOT_FOUND(HttpStatus.NOT_FOUND),

    /** Mã tổ chức (org code) đã tồn tại trong hệ thống */
    DUPLICATE_ORG_CODE(HttpStatus.CONFLICT),

    /** Người dùng đã là thành viên của tổ chức này */
    MEMBERSHIP_CONFLICT(HttpStatus.CONFLICT),

    // ─── Security ──────────────────────────────────────────────────────────
    /** Yêu cầu xác thực nhưng chưa cung cấp thông tin xác thực */
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED),

    /** Đã xác thực nhưng không có quyền thực hiện hành động này */
    FORBIDDEN(HttpStatus.FORBIDDEN),

    // ─── System ────────────────────────────────────────────────────────────
    /** Lỗi hệ thống nội bộ không xác định */
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR);

    private final HttpStatus httpStatus;

    ErrorCode(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public int getStatusValue() {
        return httpStatus.value();
    }
}
```

---

## 5. `BusinessException.java`

**Package:** `com.edu.shared.kernel.exception`

```java
package com.edu.shared.kernel.exception;

/**
 * Exception gốc cho tất cả lỗi nghiệp vụ trong domain E-Edu.
 *
 * <p>Được ném ra từ Application Services / Domain Services khi một
 * invariant nghiệp vụ bị vi phạm. {@link GlobalExceptionHandler} sẽ
 * bắt exception này và chuyển đổi thành {@code ApiErrorResponse} phù hợp.
 *
 * <p>Ví dụ sử dụng:
 * <pre>{@code
 * if (userRepository.existsByUsername(username)) {
 *     throw new BusinessException(ErrorCode.DUPLICATE_USERNAME,
 *             "Username '" + username + "' đã được sử dụng");
 * }
 * }</pre>
 */
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    /**
     * Tạo exception với mã lỗi và thông điệp mô tả.
     *
     * @param errorCode Mã lỗi nghiệp vụ, xác định HTTP status trả về
     * @param message   Mô tả chi tiết lỗi (không lộ ra production nếu INTERNAL_ERROR)
     */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Tạo exception với mã lỗi, thông điệp và nguyên nhân gốc.
     *
     * @param errorCode Mã lỗi nghiệp vụ
     * @param message   Mô tả chi tiết lỗi
     * @param cause     Nguyên nhân gốc (sẽ được log nội bộ)
     */
    public BusinessException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
```

---

## 6. `GlobalExceptionHandler.java`

**Package:** `com.edu.boot.web`

```java
package com.edu.boot.web;

import com.edu.shared.kernel.exception.BusinessException;
import com.edu.shared.kernel.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * Điểm xử lý lỗi tập trung cho toàn bộ REST API của hệ thống E-Edu.
 *
 * <p>Mọi exception không được xử lý trong Controller sẽ được bắt tại đây
 * và chuyển đổi thành {@link ApiErrorResponse} chuẩn hóa.
 *
 * <p>Thứ tự xử lý (ưu tiên từ cao đến thấp):
 * <ol>
 *   <li>{@code MethodArgumentNotValidException} — Validation Bean thất bại (422)</li>
 *   <li>{@code HttpMessageNotReadableException} — Request body không parse được (400)</li>
 *   <li>{@code BusinessException} — Lỗi nghiệp vụ domain (4xx tùy ErrorCode)</li>
 *   <li>{@code Exception} — Fallback mọi lỗi không xác định (500)</li>
 * </ol>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ─── 1. Validation Errors (422) ────────────────────────────────────────

    /**
     * Xử lý lỗi Jakarta Bean Validation.
     * Trả về danh sách {@code fieldErrors} để client biết chính xác field nào sai.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        List<ApiErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> new ApiErrorResponse.FieldError(
                        fe.getField(),
                        fe.getDefaultMessage()))
                .toList();

        log.warn("[Validation] path={} errors={}", request.getRequestURI(), fieldErrors);

        ApiErrorResponse body = ApiErrorResponse.ofValidation(
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                ErrorCode.VALIDATION_ERROR.name(),
                "Dữ liệu đầu vào không hợp lệ",
                request.getRequestURI(),
                fieldErrors
        );

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
    }

    // ─── 2. Malformed Request Body (400) ──────────────────────────────────

    /**
     * Xử lý request body không đúng JSON hoặc không thể deserialize.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        log.warn("[BadRequest] path={} message={}", request.getRequestURI(), ex.getMessage());

        ApiErrorResponse body = ApiErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "BAD_REQUEST",
                "Request body không hợp lệ hoặc thiếu",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // ─── 3. Business Exceptions (4xx) ─────────────────────────────────────

    /**
     * Xử lý tất cả lỗi nghiệp vụ từ domain layer.
     * HTTP status được xác định bởi {@link ErrorCode#getHttpStatus()}.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusiness(
            BusinessException ex,
            HttpServletRequest request) {

        ErrorCode errorCode = ex.getErrorCode();
        HttpStatus httpStatus = errorCode.getHttpStatus();

        log.warn("[Business] path={} code={} message={}",
                request.getRequestURI(), errorCode.name(), ex.getMessage());

        ApiErrorResponse body = ApiErrorResponse.of(
                httpStatus.value(),
                errorCode.name(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(httpStatus).body(body);
    }

    // ─── 4. Fallback — Unexpected Errors (500) ────────────────────────────

    /**
     * Fallback xử lý mọi exception không được bắt ở trên.
     * Không lộ stack trace hay thông tin nội bộ ra ngoài.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(
            Exception ex,
            HttpServletRequest request) {

        // Log đầy đủ stack trace nội bộ để debug
        log.error("[Unexpected] path={} exception={}", request.getRequestURI(),
                ex.getClass().getName(), ex);

        ApiErrorResponse body = ApiErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ErrorCode.INTERNAL_ERROR.name(),
                "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau.",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
```

---

## 7. Unit Tests — `GlobalExceptionHandlerTest.java`

**Package:** `com.edu.boot.web` (test source)

```java
package com.edu.boot.web;

import com.edu.shared.kernel.exception.BusinessException;
import com.edu.shared.kernel.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.*;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = GlobalExceptionHandlerTest.TestController.class)
@Import(GlobalExceptionHandler.class)
class GlobalExceptionHandlerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    // ─── Stub Controller dùng trong test ──────────────────────────────────

    @RestController
    @RequestMapping("/test")
    static class TestController {

        record SampleRequest(@jakarta.validation.constraints.NotBlank String name,
                             @jakarta.validation.constraints.Email String email) {}

        @PostMapping("/validate")
        String validate(@org.springframework.web.bind.annotation.RequestBody
                        @jakarta.validation.Valid SampleRequest req) {
            return "ok";
        }

        @PostMapping("/business")
        String business() {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "User không tồn tại");
        }

        @PostMapping("/unexpected")
        String unexpected() {
            throw new RuntimeException("Lỗi hệ thống giả lập");
        }
    }

    // ─── Test cases ────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /test/validate với body rỗng → 422 + fieldErrors")
    void whenValidationFails_return422WithFieldErrors() throws Exception {
        String invalidBody = """
                { "name": "", "email": "not-an-email" }
                """;

        mockMvc.perform(post("/test/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.fieldErrors[*].field", hasItem("name")))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/test/validate"));
    }

    @Test
    @DisplayName("POST /test/validate với body không phải JSON → 400")
    void whenBodyNotReadable_return400() throws Exception {
        mockMvc.perform(post("/test/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("NOT JSON"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    @DisplayName("POST /test/business → 404 với code USER_NOT_FOUND")
    void whenBusinessException_returnMappedStatus() throws Exception {
        mockMvc.perform(post("/test/business")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("User không tồn tại"))
                .andExpect(jsonPath("$.fieldErrors").doesNotExist());
    }

    @Test
    @DisplayName("POST /test/unexpected → 500, không lộ stack trace")
    void whenUnexpectedException_return500WithoutStackTrace() throws Exception {
        mockMvc.perform(post("/test/unexpected")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau."));
    }
}
```

---

## 8. Cấu trúc File trong Module

```
boot/
└── src/
    ├── main/java/com/edu/boot/web/
    │   ├── ApiErrorResponse.java          ← Record chuẩn hóa
    │   └── GlobalExceptionHandler.java    ← @RestControllerAdvice
    └── test/java/com/edu/boot/web/
        └── GlobalExceptionHandlerTest.java

shared-kernel/
└── src/main/java/com/edu/shared/kernel/exception/
    ├── ErrorCode.java                     ← Enum mã lỗi
    └── BusinessException.java             ← Base exception
```

---

## 9. Tiêu chí Nghiệm thu (Acceptance Criteria)

| Mã AC | Tiêu chí | Chi tiết kỹ thuật |
|:---:|---|---|
| **AC-3.1.1** | Mọi response lỗi đều theo `ApiErrorResponse` | JSON luôn có `timestamp`, `status`, `code`, `message`, `path` |
| **AC-3.1.2** | Validation lỗi trả về 422 kèm `fieldErrors` | `fieldErrors` là array, mỗi phần tử có `field` và `message` |
| **AC-3.1.3** | `BusinessException` map đúng HTTP status | `USER_NOT_FOUND` → 404, `DUPLICATE_EMAIL` → 409, v.v. |
| **AC-3.1.4** | Body JSON không hợp lệ trả 400 | Không trả 500 khi JSON malformed |
| **AC-3.1.5** | Lỗi 500 không lộ stack trace | Message luôn là generic string, stack trace chỉ in vào log |
| **AC-3.1.6** | `fieldErrors` = null với lỗi không phải validation | `@JsonInclude(NON_NULL)` đảm bảo field không xuất hiện trong JSON |
| **AC-3.1.7** | Unit test coverage ≥ 80% cho `GlobalExceptionHandler` | Tất cả 4 handler method đều có test case |
| **AC-3.1.8** | `ErrorCode` enum bao phủ mọi lỗi nghiệp vụ hiện tại | Kiểm tra danh sách enum khớp với các use case đã có |

---

## 10. Liên kết Tài liệu Liên quan

| Tài liệu | Mô tả |
|---|---|
| [Phase 3.2 — Request Validation](phase3.2.md) | Chi tiết về Bean Validation, cách `GlobalExceptionHandler` xử lý `fieldErrors` |
| [Phase 3.4 — Centralized Logging](phase3.4.md) | `TraceIdFilter` thêm `traceId` vào context, có thể include trong `ApiErrorResponse` |
| [Phase 3 README](README.md) | Index tổng thể Phase 3 |

