# Phase 3.2 — Request Validation & Error Response Chuẩn hóa

> **Dự án:** E-Edu Backend · Java 21 + Spring Boot 3.4 · Modular Monolith  
> **Tiêu chuẩn:** Jakarta Bean Validation 3.0  
> **Trạng thái:** ✅ Hoàn thành

---

## 1. Mục tiêu

Chuẩn hóa **kiểm tra dữ liệu đầu vào** (input validation) trên toàn bộ REST API thông qua Jakarta Bean Validation annotations, đảm bảo:

- Mọi lỗi validation đều được phát hiện **trước** khi vào Application/Domain layer
- Client nhận được danh sách **đầy đủ** các field bị lỗi trong một lần gọi API
- Response lỗi validation tuân theo contract `ApiErrorResponse` chuẩn hóa ở [Phase 3.1](phase3.1.md)

---

## 2. Cấu hình Dependency

### `pom.xml` (module `boot` hoặc module chứa Controller)

```xml
<!-- Jakarta Bean Validation + Hibernate Validator (implementation) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

> **Lưu ý:** `spring-boot-starter-validation` kéo theo `hibernate-validator` (implementation mặc định của Jakarta Bean Validation) và `jakarta.validation-api`. Không cần khai báo thêm version vì Spring Boot BOM quản lý.

---

## 3. Các Annotation Validation Thường Dùng

| Annotation | Áp dụng cho | Mô tả |
|---|---|---|
| `@NotNull` | Mọi kiểu | Không được null |
| `@NotBlank` | `String` | Không được null, rỗng, hoặc chỉ có khoảng trắng |
| `@NotEmpty` | `String`, `Collection`, `Array` | Không được null hoặc rỗng |
| `@Email` | `String` | Phải đúng định dạng email |
| `@Size(min, max)` | `String`, `Collection` | Độ dài / kích thước trong khoảng `[min, max]` |
| `@Min(value)` | Số | Giá trị ≥ `value` |
| `@Max(value)` | Số | Giá trị ≤ `value` |
| `@Pattern(regexp)` | `String` | Phải khớp regex |
| `@Positive` | Số | Phải > 0 |
| `@PositiveOrZero` | Số | Phải ≥ 0 |
| `@Future` | `Date`, `LocalDate` | Phải là thời điểm trong tương lai |
| `@Valid` | Object | Kích hoạt validation đệ quy (nested object) |

---

## 4. Ví dụ Request DTOs

### 4.1 `RegisterUserRequest.java`

```java
package com.edu.identity.application.dto.request;

import jakarta.validation.constraints.*;

/**
 * DTO cho yêu cầu đăng ký tài khoản mới.
 * Tất cả constraint được validate trước khi vào Application Service.
 */
public record RegisterUserRequest(

        @NotBlank(message = "Username không được để trống")
        @Size(min = 3, max = 50, message = "Username phải từ 3 đến 50 ký tự")
        @Pattern(regexp = "^[a-zA-Z0-9_]+$",
                 message = "Username chỉ được chứa chữ cái, số và dấu gạch dưới")
        String username,

        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không đúng định dạng")
        @Size(max = 255, message = "Email không được vượt quá 255 ký tự")
        String email,

        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(min = 8, max = 100, message = "Mật khẩu phải từ 8 đến 100 ký tự")
        String password,

        @NotBlank(message = "Tên hiển thị không được để trống")
        @Size(max = 100, message = "Tên hiển thị không được vượt quá 100 ký tự")
        String displayName
) {}
```

### 4.2 `CreateOrganizationRequest.java`

```java
package com.edu.organization.application.dto.request;

import jakarta.validation.constraints.*;

/**
 * DTO cho yêu cầu tạo tổ chức mới.
 */
public record CreateOrganizationRequest(

        @NotBlank(message = "Tên tổ chức không được để trống")
        @Size(min = 2, max = 200, message = "Tên tổ chức phải từ 2 đến 200 ký tự")
        String name,

        @NotBlank(message = "Mã tổ chức không được để trống")
        @Size(min = 2, max = 50, message = "Mã tổ chức phải từ 2 đến 50 ký tự")
        @Pattern(regexp = "^[A-Z0-9_]+$",
                 message = "Mã tổ chức chỉ được chứa chữ in hoa, số và dấu gạch dưới")
        String code,

        @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")
        String description,

        @NotNull(message = "Loại tổ chức không được để trống")
        OrganizationType type
) {}
```

### 4.3 Nested Object Validation

```java
package com.edu.identity.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Ví dụ DTO với nested object — dùng @Valid để validate đệ quy.
 */
public record UpdateProfileRequest(

        @NotNull(message = "Thông tin địa chỉ không được null")
        @Valid                          // ← Kích hoạt validate các field bên trong AddressDto
        AddressDto address
) {
    public record AddressDto(
            @NotBlank(message = "Thành phố không được để trống")
            String city,

            @NotBlank(message = "Tỉnh/Quốc gia không được để trống")
            String country
    ) {}
}
```

---

## 5. Cấu hình `@Validated` tại Controller

```java
package com.edu.identity.adapter.rest;

import com.edu.identity.application.dto.request.RegisterUserRequest;
import com.edu.identity.application.service.UserApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller cho các endpoint Identity.
 *
 * <p>{@code @Validated} tại class level kích hoạt validation cho các constraint
 * đặt trực tiếp trên method parameters (ví dụ: @RequestParam, @PathVariable).
 * {@code @Valid} trên @RequestBody kích hoạt Bean Validation cho object.
 */
@RestController
@RequestMapping("/api/v1/users")
@Validated  // Cần cho constraint trên method parameters (path/query variables)
public class UserController {

    private final UserApplicationService userService;

    public UserController(UserApplicationService userService) {
        this.userService = userService;
    }

    /**
     * Đăng ký tài khoản mới.
     * {@code @Valid} kích hoạt Bean Validation cho {@link RegisterUserRequest}.
     * Nếu validation thất bại → Spring tự động ném {@code MethodArgumentNotValidException}
     * → {@code GlobalExceptionHandler} bắt và trả 422.
     */
    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody @Valid RegisterUserRequest request) {
        userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Ví dụ validate PathVariable — cần @Validated tại class level.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<?> findById(
            @PathVariable
            @jakarta.validation.constraints.Positive(message = "userId phải là số dương")
            Long userId) {
        return ResponseEntity.ok(userService.findById(userId));
    }
}
```

---

## 6. Xử lý `MethodArgumentNotValidException` trong `GlobalExceptionHandler`

> Chi tiết implementation xem tại [Phase 3.1 — GlobalExceptionHandler](phase3.1.md#6-globalexceptionhandlerjava).

Luồng xử lý khi validation thất bại:

```
@Valid trên @RequestBody
        │
        ▼ (validation thất bại)
MethodArgumentNotValidException
        │
        ▼
GlobalExceptionHandler.handleValidation()
        │
        ├── Lấy danh sách FieldError từ BindingResult
        ├── Map sang List<ApiErrorResponse.FieldError>
        └── Trả ResponseEntity<ApiErrorResponse> với HTTP 422
```

### Code xử lý (trong `GlobalExceptionHandler`):

```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<ApiErrorResponse> handleValidation(
        MethodArgumentNotValidException ex,
        HttpServletRequest request) {

    // Lấy tất cả lỗi field từ BindingResult
    List<ApiErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(fe -> new ApiErrorResponse.FieldError(
                    fe.getField(),
                    fe.getDefaultMessage()))  // message từ annotation
            .toList();

    // Log cảnh báo (không phải error — đây là lỗi của client)
    log.warn("[Validation] path={} errors={}", request.getRequestURI(), fieldErrors);

    ApiErrorResponse body = ApiErrorResponse.ofValidation(
            HttpStatus.UNPROCESSABLE_ENTITY.value(),  // 422
            ErrorCode.VALIDATION_ERROR.name(),
            "Dữ liệu đầu vào không hợp lệ",
            request.getRequestURI(),
            fieldErrors
    );

    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
}
```

---

## 7. Ví dụ Request/Response

### Yêu cầu đăng ký với dữ liệu không hợp lệ

**Request:**
```http
POST /api/v1/users/register
Content-Type: application/json

{
  "username": "ab",
  "email": "not-valid-email",
  "password": "123",
  "displayName": ""
}
```

**Response — 422 Unprocessable Entity:**
```json
{
  "timestamp": "2025-11-02T10:30:00.123Z",
  "status": 422,
  "code": "VALIDATION_ERROR",
  "message": "Dữ liệu đầu vào không hợp lệ",
  "path": "/api/v1/users/register",
  "fieldErrors": [
    {
      "field": "username",
      "message": "Username phải từ 3 đến 50 ký tự"
    },
    {
      "field": "email",
      "message": "Email không đúng định dạng"
    },
    {
      "field": "password",
      "message": "Mật khẩu phải từ 8 đến 100 ký tự"
    },
    {
      "field": "displayName",
      "message": "Tên hiển thị không được để trống"
    }
  ]
}
```

> **Lưu ý quan trọng:** Tất cả lỗi trả về **trong một lần** — không yêu cầu client phải gọi nhiều lần để biết hết lỗi. Đây là behavior mặc định của `BindingResult` với Bean Validation.

---

## 8. Custom Validation Annotation (Nâng cao)

Ví dụ tạo custom annotation `@UniqueUsername` (kết hợp với DB check):

```java
package com.edu.identity.adapter.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Validate username chưa tồn tại trong database.
 * Chú ý: Đây là DB-level validation, chỉ dùng cho trường hợp UX (không thay thế
 * kiểm tra trong Application Service để tránh race condition).
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueUsernameValidator.class)
@Documented
public @interface UniqueUsername {
    String message() default "Username đã được sử dụng";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

```java
package com.edu.identity.adapter.validation;

import com.edu.identity.domain.repository.UserRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

@Component
public class UniqueUsernameValidator implements ConstraintValidator<UniqueUsername, String> {

    private final UserRepository userRepository;

    public UniqueUsernameValidator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean isValid(String username, ConstraintValidatorContext context) {
        if (username == null || username.isBlank()) {
            return true; // @NotBlank sẽ bắt lỗi này, không cần duplicate
        }
        return !userRepository.existsByUsername(username);
    }
}
```

---

## 9. Tiêu chí Nghiệm thu (Acceptance Criteria)

| Mã AC | Tiêu chí | Chi tiết kỹ thuật |
|:---:|---|---|
| **AC-3.2.1** | `spring-boot-starter-validation` được khai báo đúng | `mvn dependency:tree` thấy `hibernate-validator` |
| **AC-3.2.2** | Request DTO sử dụng đúng annotation | Mỗi field có ít nhất một constraint annotation phù hợp |
| **AC-3.2.3** | `@Valid` được đặt trên `@RequestBody` ở Controller | Không dùng manual validation trong Controller |
| **AC-3.2.4** | Validation thất bại → 422 với `fieldErrors` array | Không trả 400 hay 500 cho lỗi validation |
| **AC-3.2.5** | `fieldErrors` chứa đúng tên field và message | Field name khớp với tên field trong DTO, message từ annotation |
| **AC-3.2.6** | Tất cả lỗi trả về cùng lúc | Không cần client gọi nhiều lần |
| **AC-3.2.7** | `fieldErrors` không xuất hiện trong response lỗi không phải validation | `@JsonInclude(NON_NULL)` hoạt động đúng |
| **AC-3.2.8** | Custom message source hỗ trợ i18n (tùy chọn) | Nếu dùng `messages.properties`, message đúng theo locale |

---

## 10. Liên kết Tài liệu Liên quan

| Tài liệu | Mô tả |
|---|---|
| [Phase 3.1 — Global Exception Handling](phase3.1.md) | `GlobalExceptionHandler`, `ApiErrorResponse`, `ErrorCode` — nơi xử lý `MethodArgumentNotValidException` |
| [Phase 3 README](README.md) | Index tổng thể Phase 3 |

