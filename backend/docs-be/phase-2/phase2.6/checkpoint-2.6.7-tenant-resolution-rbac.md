# Phase 2.6: Checkpoint 2.6.7 — Tenant Resolution & RBAC

> Thuộc [Phase 2.6: Security & JWT Token Management](README.md)

## 10. Checkpoint 2.6.7: Tenant Resolution & RBAC

Mục tiêu của checkpoint 2.6.7 là phân giải Tenant Context từ header `X-Organization-Id`, xác minh tính hợp lệ của Membership qua Public Port của module `organization`, và kiểm soát quyền hạn (RBAC) ngăn chặn triệt để lỗ hổng truy cập trái phép chéo tenant.

### 10.1. TenantContext trong Shared Kernel

Vị trí: `backend/shared/shared-kernel/src/main/java/com/edu/shared/kernel/domain/tenant/TenantContext.java`

```java
package com.edu.shared.kernel.domain.tenant;

import java.util.Objects;
import java.util.Optional;
import com.edu.shared.kernel.domain.identifier.OrganizationId;
import com.edu.shared.kernel.domain.identifier.UserId;

public final class TenantContext {

    private static final ThreadLocal<TenantInfo> CURRENT_TENANT = new ThreadLocal<>();

    private TenantContext() {}

    public static void set(UserId userId, OrganizationId organizationId, String role) {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(organizationId, "organizationId must not be null");
        CURRENT_TENANT.set(new TenantInfo(userId, organizationId, role));
    }

    public static Optional<TenantInfo> get() {
        return Optional.ofNullable(CURRENT_TENANT.get());
    }

    public static TenantInfo require() {
        return get().orElseThrow(() -> new IllegalStateException("No active TenantContext found for request"));
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }

    public record TenantInfo(
        UserId userId,
        OrganizationId organizationId,
        String role
    ) {}
}
```

---

### 10.2. Public Port: `OrganizationMembershipQuery`

Module `organization` công khai hợp đồng kiểm tra quyền hạn và tư cách thành viên, bảo đảm module khác không cần import JPA Entity của nó:

Vị trí: `backend/modules/organization/src/main/java/com/edu/organization/application/port/OrganizationMembershipQuery.java`

```java
package com.edu.organization.application.port;

import java.util.Optional;
import java.util.UUID;

public interface OrganizationMembershipQuery {

    Optional<MembershipDetails> findActiveMembership(UUID userId, UUID organizationId);

    record MembershipDetails(
        UUID membershipId,
        UUID userId,
        UUID organizationId,
        String role,           // OWNER, ADMIN, MEMBER
        String status          // ACTIVE, SUSPENDED
    ) {}
}
```

---

### 10.3. Tenant Resolution Filter

Vị trí: `backend/boot/src/main/java/com/edu/boot/security/TenantResolutionFilter.java`

```java
package com.edu.boot.security;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import com.edu.organization.application.port.OrganizationMembershipQuery;
import com.edu.shared.kernel.domain.identifier.OrganizationId;
import com.edu.shared.kernel.domain.identifier.UserId;
import com.edu.shared.kernel.domain.tenant.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class TenantResolutionFilter extends OncePerRequestFilter {

    private final OrganizationMembershipQuery organizationMembershipQuery;

    public TenantResolutionFilter(OrganizationMembershipQuery organizationMembershipQuery) {
        this.organizationMembershipQuery = organizationMembershipQuery;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String orgHeader = request.getHeader("X-Organization-Id");
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (orgHeader != null && !orgHeader.isBlank() && authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
                UUID userId = UUID.fromString(jwt.getSubject());
                UUID orgId = UUID.fromString(orgHeader.trim());

                var membershipOpt = organizationMembershipQuery.findActiveMembership(userId, orgId);

                if (membershipOpt.isEmpty() || !"ACTIVE".equalsIgnoreCase(membershipOpt.get().status())) {
                    throw new AccessDeniedException("User is not an active member of organization: " + orgId);
                }

                TenantContext.set(
                    new UserId(userId),
                    new OrganizationId(orgId),
                    membershipOpt.get().role()
                );
            }

            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/v1/auth/") || path.startsWith("/actuator/");
    }
}
```

---

### 10.4. Tiêu chí Hoàn thành Checkpoint 2.6.7

| ID | Tiêu chí | Trạng thái |
| :--- | :--- | :---: |
| **AC-2.6.7.1** | `TenantContext` quản lý dữ liệu an toàn theo ThreadLocal và dọn dẹp sạch sẽ sau request | **DONE** |
| **AC-2.6.7.2** | Trích xuất `UserId` an toàn từ SecurityContext (JWT `sub`), không tin cậy request body | **DONE** |
| **AC-2.6.7.3** | Header `X-Organization-Id` được xác minh qua `OrganizationMembershipQuery` | **DONE** |
| **AC-2.6.7.4** | Người dùng không thuộc tổ chức hoặc membership bị đình chỉ bị từ chối với lỗi 403 | **DONE** |
| **AC-2.6.7.5** | Vai trò (`OWNER`, `ADMIN`, `MEMBER`) được nạp vào context phục vụ đánh giá RBAC | **DONE** |

---

