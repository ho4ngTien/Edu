# Phase 7.1 — Unit Tests & WebMvc Mock Slice Tests

Tài liệu đặc tả chiến lược kiểm thử, các lớp test hiện có và tiêu chí nghiệm thu cho phân hệ **Kiểm thử Đơn vị và Slice Tests** của backend E-Edu.

---

## 1. Chiến lược Kiểm thử (Test Pyramid)

```
         ╔══════════════╗
         ║  E2E / API   ║  ← Testcontainers (Phase 7.3)
         ╠══════════════╣
         ║  Integration  ║  ← @SpringBootTest + MockMvc
         ╠══════════════╣
         ║  WebMvc Slice ║  ← @WebMvcTest (nhanh, cô lập)
         ╠══════════════╣
         ║  Unit Tests   ║  ← JUnit 5 + Mockito (nhanh nhất)
         ╚══════════════╝
```

**Nguyên tắc chính**:
- **Unit Tests**: Kiểm thử logic nghiệp vụ thuần túy (Service, Domain classes) bằng Mockito — không khởi động Spring context.
- **WebMvc Slice Tests**: `@WebMvcTest` chỉ tải `Controller` layer, mock toàn bộ dependencies — kiểm tra HTTP binding, validation, serialization nhanh.
- **Không test implementation details**: Test các hành vi (behavior), không test cấu trúc nội bộ.

---

## 2. Tiêu chí Nghiệm thu (Acceptance Criteria)

| Mã AC | Tiêu chí | Chi tiết kỹ thuật |
| :---: | :--- | :--- |
| **AC-7.1.1** | **Unit Tests Coverage cho Service Layer** | `IdentityServiceTest`, `OrganizationServiceTest`, `AuthenticationServiceTest` cover các happy path và error cases chính. |
| **AC-7.1.2** | **WebMvc Slice Tests cho Controllers** | `AccountControllerTest`, `AuthenticationControllerTest`, `OrganizationControllerTest` dùng `@WebMvcTest` + `MockMvc`. |
| **AC-7.1.3** | **Validation Kiểm thử trong Controller Tests** | Test cases xác nhận validation errors (field errors 422) được trả đúng format `ApiErrorResponse`. |
| **AC-7.1.4** | **Security Context Mocking** | Controller tests cho endpoints cần xác thực sử dụng `@WithMockUser` hoặc mock `SecurityContext`. |
| **AC-7.1.5** | **`mvn clean test` Pass Toàn bộ** | Lệnh `mvn clean test` trên toàn bộ reactor 19 modules phải `BUILD SUCCESS` mà không có test failure nào. |

---

## 3. Cấu hình Test Profile

```yaml
# backend/boot/src/test/resources/application-test.yml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: false
  flyway:
    enabled: false  # Tắt Flyway trong unit tests — dùng H2 in-memory

edu:
  security:
    jwt:
      secret-base64: dGVzdC1zZWNyZXQta2V5LWZvci10ZXN0aW5nLW9ubHktMzIwLWJpdA==
      issuer: e-edu-test
      audience: e-edu-clients
      access-token-ttl: PT15M
      refresh-token-ttl: P7D
```

---

## 4. Unit Test — IdentityService

```java
// com.edu.identity.application.service.IdentityServiceTest
@ExtendWith(MockitoExtension.class)
class IdentityServiceTest {

    @Mock
    private UserJpaRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private IdentityService identityService;

    @Test
    @DisplayName("registerAccount: should encode password and save user when valid input")
    void registerAccount_validInput_savesUserWithEncodedPassword() {
        // Arrange
        RegisterAccountCommand command = new RegisterAccountCommand(
                "teacher.nguyen", "teacher@edu.vn", "RawPassword123!"
        );
        when(userRepository.existsByUsername("teacher.nguyen")).thenReturn(false);
        when(userRepository.existsByEmail("teacher@edu.vn")).thenReturn(false);
        when(passwordEncoder.encode("RawPassword123!")).thenReturn("$2a$10$hashedPassword");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        AccountView result = identityService.registerAccount(command);

        // Assert
        assertThat(result.username()).isEqualTo("teacher.nguyen");
        assertThat(result.email()).isEqualTo("teacher@edu.vn");
        assertThat(result.status()).isEqualTo("ACTIVE");

        // Xác nhận password đã được mã hóa và không bao giờ lộ raw password
        verify(passwordEncoder).encode("RawPassword123!");
        verify(userRepository).save(argThat(user ->
                user.getPasswordHash().equals("$2a$10$hashedPassword")
        ));
    }

    @Test
    @DisplayName("registerAccount: should throw DuplicateUsernameException when username taken")
    void registerAccount_duplicateUsername_throwsException() {
        // Arrange
        RegisterAccountCommand command = new RegisterAccountCommand(
                "existing.user", "new@edu.vn", "Password123!"
        );
        when(userRepository.existsByUsername("existing.user")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> identityService.registerAccount(command))
                .isInstanceOf(DuplicateUsernameException.class)
                .hasMessageContaining("existing.user");

        // Xác nhận không gọi passwordEncoder khi username đã tồn tại
        verifyNoInteractions(passwordEncoder);
        verify(userRepository, never()).save(any());
    }
}
```

---

## 5. WebMvc Slice Test — AccountController

```java
// com.edu.identity.infrastructure.web.AccountControllerTest
@WebMvcTest(AccountController.class)
@Import(SecurityTestConfig.class)  // Config đặc biệt cho test: tắt security filter
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IdentityService identityService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /register: 201 Created when valid request")
    void register_validRequest_returns201() throws Exception {
        // Arrange
        AccountView mockView = new AccountView(
                UUID.randomUUID(), "teacher.nguyen", "teacher@edu.vn", "ACTIVE",
                Instant.now()
        );
        when(identityService.registerAccount(any())).thenReturn(mockView);

        RegisterAccountRequest request = new RegisterAccountRequest(
                "teacher.nguyen", "teacher@edu.vn", "SecurePass123!"
        );

        // Act & Assert
        mockMvc.perform(post("/api/v1/identity/accounts/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("teacher.nguyen"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());  // ArchUnit AR-09
    }

    @Test
    @DisplayName("POST /register: 422 Unprocessable Entity when blank username")
    void register_blankUsername_returns422WithFieldError() throws Exception {
        RegisterAccountRequest request = new RegisterAccountRequest("", "email@edu.vn", "Pass123!");

        mockMvc.perform(post("/api/v1/identity/accounts/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("username"));
    }
}
```

---

## 6. Liên kết Tài liệu Liên quan

- [Phase 7 README](README.md) — Tổng quan phân hệ Quality & Delivery
- [Phase 7.2: Architecture Tests với ArchUnit](phase7.2.md) — Kiểm soát ranh giới module
- [Phase 7.3: Integration Tests với Testcontainers](phase7.3.md) — E2E tests với PostgreSQL thật
