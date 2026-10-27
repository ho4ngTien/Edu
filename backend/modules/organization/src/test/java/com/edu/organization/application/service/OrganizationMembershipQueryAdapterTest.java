package com.edu.organization.application.service;

import com.edu.organization.application.port.OrganizationMembershipQuery;
import com.edu.organization.domain.model.MembershipStatus;
import com.edu.organization.domain.model.OrganizationRole;
import com.edu.organization.infrastructure.persistence.entity.OrganizationMembershipEntity;
import com.edu.organization.infrastructure.persistence.repository.OrganizationMembershipJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class OrganizationMembershipQueryAdapterTest {

    @Mock
    private OrganizationMembershipJpaRepository membershipRepository;

    private OrganizationMembershipQueryAdapter queryAdapter;

    @BeforeEach
    void setUp() {
        queryAdapter = new OrganizationMembershipQueryAdapter(membershipRepository);
    }

    @Test
    @DisplayName("findActiveMembership returns details when membership is ACTIVE")
    void findActiveMembershipSuccess() {
        UUID userId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();
        UUID membershipId = UUID.randomUUID();

        OrganizationMembershipEntity entity = new OrganizationMembershipEntity(
                membershipId,
                orgId,
                userId,
                MembershipStatus.ACTIVE,
                OrganizationRole.ADMIN,
                Instant.now()
        );

        given(membershipRepository.findByOrganizationIdAndUserId(orgId, userId))
                .willReturn(Optional.of(entity));

        Optional<OrganizationMembershipQuery.MembershipDetails> result = queryAdapter.findActiveMembership(userId, orgId);

        assertThat(result).isPresent();
        assertThat(result.get().membershipId()).isEqualTo(membershipId);
        assertThat(result.get().role()).isEqualTo("ADMIN");
        assertThat(result.get().status()).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("findActiveMembership returns empty when membership is SUSPENDED")
    void findActiveMembershipSuspended() {
        UUID userId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();

        OrganizationMembershipEntity entity = new OrganizationMembershipEntity(
                UUID.randomUUID(),
                orgId,
                userId,
                MembershipStatus.SUSPENDED,
                OrganizationRole.MEMBER,
                Instant.now()
        );

        given(membershipRepository.findByOrganizationIdAndUserId(orgId, userId))
                .willReturn(Optional.of(entity));

        Optional<OrganizationMembershipQuery.MembershipDetails> result = queryAdapter.findActiveMembership(userId, orgId);

        assertThat(result).isEmpty();
    }
}
