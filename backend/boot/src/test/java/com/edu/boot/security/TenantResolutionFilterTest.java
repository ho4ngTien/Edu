package com.edu.boot.security;

import com.edu.organization.application.port.OrganizationMembershipQuery;
import com.edu.shared.kernel.domain.tenant.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TenantResolutionFilterTest {

    @Mock
    private OrganizationMembershipQuery organizationMembershipQuery;

    @Mock
    private FilterChain filterChain;

    @AfterEach
    void tearDown() {
        TenantContext.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Active membership sets TenantContext and clears it after request")
    void activeMembershipSetsTenantContext() throws ServletException, IOException {
        TenantResolutionFilter filter = new TenantResolutionFilter(organizationMembershipQuery);

        UUID userId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();

        Jwt jwt = Jwt.withTokenValue("mock-token")
                .header("alg", "HS256")
                .subject(userId.toString())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(900))
                .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));

        given(organizationMembershipQuery.findActiveMembership(userId, orgId))
                .willReturn(Optional.of(new OrganizationMembershipQuery.MembershipDetails(
                        UUID.randomUUID(), userId, orgId, "ADMIN", "ACTIVE"
                )));

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/courses");
        request.addHeader("X-Organization-Id", orgId.toString());
        MockHttpServletResponse response = new MockHttpServletResponse();

        AtomicBoolean verifiedInsideChain = new AtomicBoolean(false);
        FilterChain testChain = (req, res) -> {
            assertThat(TenantContext.get().map(TenantContext.TenantInfo::organizationId).map(id -> id.value()).orElse(null)).isEqualTo(orgId);
            assertThat(TenantContext.get().map(TenantContext.TenantInfo::userId).map(id -> id.value()).orElse(null)).isEqualTo(userId);
            assertThat(TenantContext.get().map(TenantContext.TenantInfo::role).orElse(null)).isEqualTo("ADMIN");
            verifiedInsideChain.set(true);
        };

        filter.doFilter(request, response, testChain);

        assertThat(verifiedInsideChain.get()).isTrue();
        assertThat(TenantContext.get()).isEmpty();
    }

    @Test
    @DisplayName("Inactive or missing membership throws AccessDeniedException")
    void nonMemberThrowsAccessDenied() {
        TenantResolutionFilter filter = new TenantResolutionFilter(organizationMembershipQuery);

        UUID userId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();

        Jwt jwt = Jwt.withTokenValue("mock-token")
                .header("alg", "HS256")
                .subject(userId.toString())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(900))
                .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));

        given(organizationMembershipQuery.findActiveMembership(userId, orgId))
                .willReturn(Optional.empty());

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/courses");
        request.addHeader("X-Organization-Id", orgId.toString());
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThatThrownBy(() -> filter.doFilter(request, response, filterChain))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("User is not an active member of organization");

        assertThat(TenantContext.get()).isEmpty();
    }

    @Test
    @DisplayName("Request without X-Organization-Id passes through filterChain")
    void noHeaderPassesThrough() throws ServletException, IOException {
        TenantResolutionFilter filter = new TenantResolutionFilter(organizationMembershipQuery);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/public-info");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(TenantContext.get()).isEmpty();
    }
}
