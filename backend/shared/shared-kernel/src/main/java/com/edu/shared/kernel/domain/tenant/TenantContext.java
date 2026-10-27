package com.edu.shared.kernel.domain.tenant;

import com.edu.shared.kernel.domain.identifier.OrganizationId;
import com.edu.shared.kernel.domain.identifier.UserId;

import java.util.Objects;
import java.util.Optional;

public final class TenantContext {

    private static final ThreadLocal<TenantInfo> CURRENT_TENANT = new ThreadLocal<>();

    private TenantContext() {
    }

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
    ) {
    }
}
