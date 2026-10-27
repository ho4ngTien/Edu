package com.edu.boot.security;

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
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

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
