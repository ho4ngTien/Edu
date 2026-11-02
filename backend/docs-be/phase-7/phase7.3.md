# Phase 7.3 — Integration Tests với PostgreSQL Testcontainers

Tài liệu đặc tả chiến lược, cấu hình và các test cases cho **Integration Tests** sử dụng PostgreSQL Testcontainers — kiểm thử end-to-end với cơ sở dữ liệu thật trong môi trường cô lập.

---

## 1. Mục tiêu & Sự khác biệt với Unit Tests

| | Unit Tests (Phase 7.1) | Integration Tests (Phase 7.3) |
| :--- | :--- | :--- |
| **Tốc độ** | Cực nhanh (< 1ms/test) | Chậm hơn (1-5s/test) |
| **Database** | H2 In-memory hoặc Mock | PostgreSQL 15 thật (container) |
| **Spring Context** | Không / Slice chỉ Web layer | `@SpringBootTest` đầy đủ |
| **Flyway** | Tắt | Chạy đầy đủ V1-V5+ |
| **Mục đích** | Logic nghiệp vụ | Tích hợp DB, API đầu-cuối |

**Khi nào dùng Integration Tests?**
- Test các migration SQL phức tạp
- Test full authentication flow (login → refresh → logout)
- Test tenant isolation (dữ liệu Org A không lộ ra Org B)
- Test các query JPA với điều kiện phức tạp

---

## 2. Tiêu chí Nghiệm thu (Acceptance Criteria)

| Mã AC | Tiêu chí | Chi tiết kỹ thuật |
| :---: | :--- | :--- |
| **AC-7.3.1** | **Testcontainers Setup** | `PostgreSQLContainer` khởi động với image `postgres:15-alpine`. Tự động tạo schemas `identity` và `organization` qua Flyway migration. |
| **AC-7.3.2** | **Full Auth Flow Test** | Test end-to-end: đăng ký tài khoản → đăng nhập → lấy access token → refresh → logout → xác nhận refresh token bị revoke. |
| **AC-7.3.3** | **Tenant Isolation Test** | Tạo 2 tổ chức (Org A, Org B). Xác nhận member của Org A không thể truy cập dữ liệu của Org B (trả 403 hoặc 404). |
| **AC-7.3.4** | **Refresh Token Reuse Detection Test** | Sau khi refresh thành công, gửi lại refresh token cũ → xác nhận hệ thống revoke toàn bộ family và trả 401. |
| **AC-7.3.5** | **Build Isolation** | Integration tests chạy trong profile `integration-test`, không ảnh hưởng đến unit tests thông thường (`mvn test` vs `mvn verify`). |

---

## 3. Maven Dependencies

```xml
<!-- tests/integration-tests/pom.xml -->
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-testcontainers</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>postgresql</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>junit-jupiter</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

---

## 4. Base Integration Test Class

```java
// com.edu.integration.BaseIntegrationTest
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("edu_test")
            .withUsername("edu_user")
            .withPassword("edu_password")
            .withReuse(true);  // Tái sử dụng container giữa các test class để tăng tốc

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected ObjectMapper objectMapper;
}
```

---

## 5. Full Authentication Flow Integration Test

```java
// com.edu.integration.auth.AuthenticationFlowIntegrationTest
class AuthenticationFlowIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("Full auth flow: register → login → refresh → logout")
    void fullAuthenticationFlow() throws Exception {
        // Step 1: Đăng ký tài khoản
        RegisterAccountRequest registerRequest = new RegisterAccountRequest(
                "it.user." + System.nanoTime(), // Tên duy nhất để tránh conflict
                "it.user." + System.nanoTime() + "@test.com",
                "SecurePass123!"
        );

        ResponseEntity<AccountView> registerResponse = restTemplate.postForEntity(
                "/api/v1/identity/accounts/register",
                registerRequest, AccountView.class
        );
        assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(registerResponse.getBody()).isNotNull();

        // Step 2: Đăng nhập
        LoginRequest loginRequest = new LoginRequest(
                registerRequest.username(), registerRequest.password()
        );
        ResponseEntity<TokenResponse> loginResponse = restTemplate.postForEntity(
                "/api/v1/auth/login", loginRequest, TokenResponse.class
        );
        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        TokenResponse tokens = loginResponse.getBody();
        assertThat(tokens).isNotNull();
        assertThat(tokens.accessToken()).isNotBlank();
        assertThat(tokens.refreshToken()).isNotBlank();

        // Step 3: Refresh token
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest(tokens.refreshToken());
        ResponseEntity<TokenResponse> refreshResponse = restTemplate.postForEntity(
                "/api/v1/auth/refresh", refreshRequest, TokenResponse.class
        );
        assertThat(refreshResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        TokenResponse newTokens = refreshResponse.getBody();
        assertThat(newTokens.refreshToken()).isNotEqualTo(tokens.refreshToken()); // Token mới khác cũ

        // Step 4: Reuse Detection — gửi lại refresh token cũ → phải bị từ chối
        ResponseEntity<ApiErrorResponse> reuseResponse = restTemplate.postForEntity(
                "/api/v1/auth/refresh", refreshRequest, ApiErrorResponse.class
        );
        assertThat(reuseResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(reuseResponse.getBody().code()).isEqualTo("REFRESH_TOKEN_REUSED");

        // Step 5: Logout
        LogoutRequest logoutRequest = new LogoutRequest(newTokens.refreshToken());
        ResponseEntity<Void> logoutResponse = restTemplate.postForEntity(
                "/api/v1/auth/logout", logoutRequest, Void.class
        );
        assertThat(logoutResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}
```

---

## 6. Tenant Isolation Integration Test

```java
// com.edu.integration.multitenancy.TenantIsolationIntegrationTest
class TenantIsolationIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("Member của Org A không được truy cập dữ liệu của Org B")
    void orgAMemberCannotAccessOrgBData() {
        // Setup: Tạo 2 tổ chức với owner khác nhau
        String orgAToken = setupOrgAndGetToken("ORG-A-" + System.nanoTime());
        String orgBToken = setupOrgAndGetToken("ORG-B-" + System.nanoTime());
        UUID orgBId = getOrgId(orgBToken);

        // Act: Dùng token của Org A để truy cập với Organization ID của Org B
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(orgAToken);
        headers.set("X-Organization-Id", orgBId.toString());

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/organizations/" + orgBId + "/members",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        // Assert: Phải bị từ chối (403 vì không có membership)
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
```

---

## 7. Cấu hình application-test.yml cho Integration Tests

```yaml
spring:
  flyway:
    enabled: true   # Flyway chạy đầy đủ với container PostgreSQL
    locations: classpath:db/migration
  jpa:
    hibernate:
      ddl-auto: validate

edu:
  security:
    jwt:
      secret-base64: dGVzdC1zZWNyZXQta2V5LWZvci1pbnRlZ3JhdGlvbi10ZXN0cy0zMjBiaXQ=
      issuer: e-edu-integration-test
      audience: e-edu-clients
      access-token-ttl: PT15M
      refresh-token-ttl: P7D

logging:
  level:
    com.edu: DEBUG
    org.flywaydb: INFO
```

---

## 8. Liên kết Tài liệu Liên quan

- [Phase 7 README](README.md) — Tổng quan phân hệ Quality & Delivery
- [Phase 7.1: Unit & Slice Tests](phase7.1.md) — Tầng kiểm thử nhanh
- [Phase 7.5: CI/CD Pipeline](phase7.5.md) — Tích hợp integration tests vào GitHub Actions

