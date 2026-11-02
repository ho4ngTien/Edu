# Phase 7.4 — OpenAPI / Swagger Documentation

Tài liệu đặc tả cấu hình, quy chuẩn annotation và tiêu chí nghiệm thu cho **OpenAPI 3.0 Documentation** tự động sinh từ mã nguồn bằng `springdoc-openapi`.

---

## 1. Mục tiêu & Phạm vi

- **Tài liệu API tự động**: Swagger UI tự sinh từ annotations trong Controller code, luôn đồng bộ với implementation thực tế — không có tài liệu cũ lỗi thời.
- **Bearer JWT Security Scheme**: Developers và testers có thể nhập JWT token trực tiếp vào Swagger UI để test API mà không cần tool bên ngoài.
- **Contract-First Ready**: OpenAPI spec có thể export thành `openapi.json` để dùng cho code generation (frontend client SDK, API gateway config).
- **Nhóm API theo Tag**: Các endpoint được nhóm theo module (`Identity`, `Authentication`, `Organization`) cho dễ điều hướng.

---

## 2. Tiêu chí Nghiệm thu (Acceptance Criteria)

| Mã AC | Tiêu chí | Chi tiết kỹ thuật |
| :---: | :--- | :--- |
| **AC-7.4.1** | **Swagger UI Truy cập được** | `GET /swagger-ui.html` trả HTTP 200 và hiển thị giao diện Swagger UI. |
| **AC-7.4.2** | **OpenAPI JSON Spec** | `GET /v3/api-docs` trả OpenAPI 3.0 JSON spec với đầy đủ endpoint, schema, security definitions. |
| **AC-7.4.3** | **Bearer JWT Security Scheme** | Swagger UI có nút "Authorize" cho phép nhập Bearer token và gọi API trực tiếp. |
| **AC-7.4.4** | **API Grouping theo Tag** | Mỗi Controller có `@Tag(name = "...")` phù hợp. Swagger UI hiển thị nhóm: Authentication, Identity Accounts, Organizations, Members. |
| **AC-7.4.5** | **Response Schema Đầy đủ** | Mỗi endpoint có `@ApiResponse` cho HTTP 200/201, 400, 401, 403, 404, 422 với schema `ApiErrorResponse` cho error cases. |
| **AC-7.4.6** | **Public Endpoint Bypass Security** | Swagger UI và `/v3/api-docs` được thêm vào whitelist trong `SecurityConfig` (`permitAll`). |

---

## 3. Maven Dependency

```xml
<!-- boot/pom.xml -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.8.0</version>
</dependency>
```

---

## 4. Cấu hình `OpenApiConfig.java`

```java
// com.edu.boot.config.OpenApiConfig
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI eEduOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("E-Edu Backend API")
                        .description("REST API documentation cho nền tảng quản lý giáo dục E-Edu.\n\n" +
                                "**Authentication**: Sử dụng Bearer JWT token. Đăng nhập tại `POST /api/v1/auth/login` " +
                                "để lấy `accessToken`, sau đó nhập vào ô Authorize phía trên.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("E-Edu Backend Team")
                                .email("backend@e-edu.vn"))
                        .license(new License().name("Proprietary")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .name("bearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Nhập JWT Access Token (không cần tiền tố 'Bearer ')")));
    }
}
```

---

## 5. Cấu hình application.yml

```yaml
springdoc:
  api-docs:
    path: /v3/api-docs
    enabled: true
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
    operations-sorter: method
    tags-sorter: alpha
    display-request-duration: true
    try-it-out-enabled: true
  show-actuator: false  # Ẩn actuator endpoints khỏi Swagger UI
```

---

## 6. Annotation Chuẩn trên Controllers

```java
// Ví dụ đầy đủ cho AuthenticationController
@Tag(name = "Authentication", description = "Xác thực người dùng: đăng nhập, làm mới token, đăng xuất")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    @Operation(
        summary = "Đăng nhập",
        description = "Xác thực tài khoản và cấp phát JWT Access Token + Refresh Token"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Đăng nhập thành công",
            content = @Content(schema = @Schema(implementation = TokenResponse.class))),
        @ApiResponse(responseCode = "401", description = "Sai thông tin đăng nhập",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "422", description = "Dữ liệu đầu vào không hợp lệ",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authenticationService.authenticate(new LoginCommand(request.username(), request.password()));
    }

    @Operation(
        summary = "Làm mới Access Token",
        description = "Dùng Refresh Token để lấy bộ token mới. Refresh Token cũ bị vô hiệu hóa ngay lập tức (Rotation)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cấp token mới thành công"),
        @ApiResponse(responseCode = "401", description = "Refresh Token không hợp lệ, hết hạn, hoặc đã bị dùng lại")
    })
    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.OK)
    public TokenResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return authenticationService.refreshToken(request.refreshToken());
    }
}
```

---

## 7. Schema Documentation trên DTOs

```java
// Tài liệu hóa Request/Response Record với @Schema
public record LoginRequest(
    @Schema(description = "Tên đăng nhập (username)", example = "teacher.nguyen", requiredMode = REQUIRED)
    @NotBlank String username,

    @Schema(description = "Mật khẩu", example = "SecurePassword123!", requiredMode = REQUIRED)
    @NotBlank String password
) {}

public record TokenResponse(
    @Schema(description = "JWT Access Token (Bearer)", example = "eyJhbGciOiJIUzI1NiJ9...")
    String accessToken,

    @Schema(description = "Opaque Refresh Token (lưu trữ an toàn)")
    String refreshToken,

    @Schema(description = "Thời hạn access token tính bằng giây", example = "900")
    long expiresIn,

    @Schema(description = "Luôn là 'Bearer'", example = "Bearer")
    String tokenType
) {}
```

---

## 8. Liên kết Tài liệu Liên quan

- [Phase 7 README](README.md) — Tổng quan phân hệ Quality & Delivery
- [Phase 7.5: CI/CD Pipeline](phase7.5.md) — Export OpenAPI spec trong CI
- [Phase 3.1: Global Exception Handling](../phase-3/phase3.1.md) — Schema `ApiErrorResponse`
