package com.edu.organization.application.service;

import com.edu.organization.application.port.OrganizationMembershipQuery;
import com.edu.organization.infrastructure.persistence.repository.OrganizationMembershipJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class OrganizationMembershipQueryAdapter implements OrganizationMembershipQuery {

    private final OrganizationMembershipJpaRepository membershipRepository;

    public OrganizationMembershipQueryAdapter(OrganizationMembershipJpaRepository membershipRepository) {
        this.membershipRepository = membershipRepository;
    }

    @Override
    public Optional<MembershipDetails> findActiveMembership(UUID userId, UUID organizationId) {
        return membershipRepository.findByOrganizationIdAndUserId(organizationId, userId)
                .filter(m -> "ACTIVE".equalsIgnoreCase(m.getStatus().name()))
                .map(m -> new MembershipDetails(
                        m.getId(),
                        m.getUserId(),
                        m.getOrganizationId(),
                        m.getRole().name(),
                        m.getStatus().name()
                ));
    }
}
