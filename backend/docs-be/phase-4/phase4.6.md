# Phase 4.6 — Tenant Resolution Context (`X-Organization-Id`, `TenantResolutionFilter`, `TenantContext`)

Tài liệu thiết kế kiến trúc, đặc tả kỹ thuật và tiêu chí nghiệm thu cho cơ chế **Tenant Resolution** trong kiến trúc Multi-Tenancy. Hệ thống xác định ngữ cảnh tổ chức của mỗi request đã xác thực thông qua HTTP header `X-Organization-Id` và lưu trữ trong `TenantContext` (ThreadLocal).

---

## 1. Mục tiêu & Nguyên lý Thiết kế

Trong kiến trúc **Modular Monolith Multi-Tenancy**, mỗi request thao tác nghiệp vụ cần xác định rõ:
- **Ai** đang gửi request (`userId` từ JWT Access Token trong `SecurityContext`)
- **Của tổ chức nào** (`organizationId` từ header `X-Organization-Id`)
- **Có membership hợp lệ không** (xác minh qua Public Port `OrganizationMembershipQuery`)

**Luồng kiểm tra 3 tầng bắt buộc** (Three-Gate Tenant Validation):
1. **Gate 1 — JWT Authentication**: Request phải có Bearer Token hợp lệ (xử lý bởi Spring Security OAuth2 Resource Server Filter).
2. **Gate 2 — Tenant Header Extraction**: Header `X-Organization-Id` phải có mặt, hợp lệ UUID, và tổ chức phải ở trạng thái `ACTIVE`.
3. **Gate 3 — Membership Validation**: User (trích từ JWT) phải có membership `ACTIVE` trong tổ chức được chỉ định.

---

## 2. Tiêu chí Nghiệm thu (Acceptance Criteria)

| Mã AC | Tiêu chí | Chi tiết kỹ thuật |
| :---: | :--- | :--- |
| **AC-4.6.1** | **`TenantContext` Thread-Local Storage** | Class `TenantContext` trong `shared-kernel` cung cấp `setOrganizationId()`, `getOrganizationId()`, và `clear()` dựa trên `ThreadLocal`. |
| **AC-4.6.2** | **`TenantResolutionFilter` Lifecycle** | Filter chạy sau `BearerTokenAuthenticationFilter` (chỉ áp dụng cho request đã xác thực). Đọc `X-Organization-Id`, validate UUID format, đặt vào `TenantContext`. Đảm bảo `TenantContext.clear()` trong `finally`. |
| **AC-4.6.3** | **Membership Validation qua Port** | Filter gọi `OrganizationMembershipQuery.hasActiveMembership(userId, organizationId)`. Từ chối request với HTTP 403 nếu user không có membership `ACTIVE`. |
| **AC-4.6.4** | **Bypass cho Public Endpoints** | Filter không áp dụng cho các public endpoint (`/api/v1/auth/**`, `/actuator/**`). Sử dụng danh sách whitelist pattern hoặc kiểm tra `SecurityContextHolder`. |
| **AC-4.6.5** | **Xử lý Lỗi Chuẩn hóa** | Thiếu header → 400 Bad Request (`MISSING_TENANT_HEADER`). UUID không hợp lệ → 400 (`INVALID_TENANT_ID`). Không có membership → 403 Forbidden (`TENANT_ACCESS_DENIED`). |

---

## 3. Thiết kế `TenantContext` (Shared Kernel)

```java
// com.edu.shared.kernel.context.TenantContext
public final class TenantContext {

    private static final ThreadLocal<OrganizationId> CURRENT_ORGANIZATION = new ThreadLocal<>();

    private TenantContext() {
        // Utility class — không cho phép khởi tạo
    }

    public static void setOrganizationId(OrganizationId organizationId) {
        CURRENT_ORGANIZATION.set(organizationId);
    }

    public static OrganizationId getOrganizationId() {
        OrganizationId orgId = CURRENT_ORGANIZATION.get();
        if (orgId == null) {
            throw new TenantContextNotFoundException("Tenant context not set. Ensure TenantResolutionFilter is active.");
        }
        return orgId;
    }

    public static Optional<OrganizationId> tryGetOrganizationId() {
        return Optional.ofNullable(CURRENT_ORGANIZATION.get());
    }

    public static void clear() {
        CURRENT_ORGANIZATION.remove();
    }
}
```

---

## 4. Triển khai `TenantResolutionFilter` (Boot Module)

```java
// com.edu.boot.security.TenantResolutionFilter
@Component
public class TenantResolutionFilter extends OncePerRequestFilter {

    private static final String TENANT_HEADER = "X-Organization-Id";

    private final OrganizationMembershipQuery membershipQuery;
    private final ObjectMapper objectMapper;

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    private static final List<String> BYPASS_PATTERNS = List.of(
            "/api/v1/auth/**",
            "/api/v1/identity/accounts/register",
            "/actuator/**",
            "/v3/api-docs/**",
            "/swagger-ui/**"
    );

    public TenantResolutionFilter(OrganizationMembershipQuery membershipQuery, ObjectMapper objectMapper) {
        this.membershipQuery = membershipQuery;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return BYPASS_PATTERNS.stream().anyMatch(pattern -> PATH_MATCHER.match(pattern, path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // Chỉ xử lý khi đã xác thực
            if (authentication == null || !authentication.isAuthenticated()) {
                filterChain.doFilter(request, response);
                return;
            }

            String tenantHeader = request.getHeader(TENANT_HEADER);
            if (tenantHeader == null || tenantHeader.isBlank()) {
                writeErrorResponse(response, HttpStatus.BAD_REQUEST, "MISSING_TENANT_HEADER",
                        "Header X-Organization-Id is required", request.getRequestURI());
                return;
            }

            UUID organizationUUID;
            try {
                organizationUUID = UUID.fromString(tenantHeader);
            } catch (IllegalArgumentException e) {
                writeErrorResponse(response, HttpStatus.BAD_REQUEST, "INVALID_TENANT_ID",
                        "Invalid organization ID format", request.getRequestURI());
                return;
            }

            OrganizationId organizationId = new OrganizationId(organizationUUID);
            UserId userId = extractUserId(authentication);

            boolean hasMembership = membershipQuery.hasActiveMembership(userId, organizationId);
            if (!hasMembership) {
                writeErrorResponse(response, HttpStatus.FORBIDDEN, "TENANT_ACCESS_DENIED",
                        "You do not have access to this organization", request.getRequestURI());
                return;
            }

            TenantContext.setOrganizationId(organizationId);
            filterChain.doFilter(request, response);

        } finally {
            TenantContext.clear();  // Đảm bảo luôn xóa sau request
        }
    }

    private UserId extractUserId(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String subject = jwt.getSubject();
        return new UserId(UUID.fromString(subject));
    }

    private void writeErrorResponse(HttpServletResponse response, HttpStatus status,
                                    String code, String message, String path) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiErrorResponse errorResponse = new ApiErrorResponse(
                Instant.now(), status.value(), code, message, path, null
        );
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
```

---

## 5. Port Interface cho Tenant Membership

```java
// com.edu.organization.application.port.OrganizationMembershipQuery
public interface OrganizationMembershipQuery {

    /**
     * Kiểm tra user có membership ACTIVE trong organization hay không.
     * Được gọi bởi TenantResolutionFilter trước khi xử lý request.
     */
    boolean hasActiveMembership(UserId userId, OrganizationId organizationId);

    /**
     * Lấy danh sách vai trò (roles) của user trong một organization.
     * Được dùng bởi method-level security để kiểm tra quyền cụ thể.
     */
    Set<String> getUserRolesInOrganization(UserId userId, OrganizationId organizationId);
}
```

---

## 6. Sử dụng TenantContext trong Service Layer

```java
// Ví dụ: Tự động áp dụng tenant filter trong mọi query nghiệp vụ
@Service
@Transactional(readOnly = true)
public class EnrollmentQueryService {

    private final EnrollmentRepository enrollmentRepository;

    public List<EnrollmentView> findEnrollmentsForCurrentTenant() {
        OrganizationId orgId = TenantContext.getOrganizationId(); // Lấy từ ThreadLocal
        return enrollmentRepository.findAllByOrganizationId(orgId.value())
                .stream()
                .map(this::mapToView)
                .toList();
    }
}
```

---

## 7. Liên kết Tài liệu Liên quan

- [Phase 4 README](README.md) — Tổng quan phân hệ Identity & Access
- [Phase 4.4: JWT Token Management](phase4.4.md) — SecurityFilterChain và JWT xác thực
- [Phase 4.5: Authentication Endpoints](phase4.5.md) — Login / Refresh / Logout APIs
- [Phase 4.7: Dynamic RBAC & Authorization](phase4.7.md) — Method-level và Resource-level authorization
