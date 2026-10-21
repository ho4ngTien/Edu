package com.edu.organization.application.service;

import com.edu.organization.api.dto.MembershipView;
import com.edu.organization.application.command.InviteMemberCommand;
import com.edu.organization.application.command.UpdateMemberStatusCommand;
import com.edu.organization.application.port.OrganizationAccountQuery;
import com.edu.organization.domain.exception.MembershipConflictException;
import com.edu.organization.domain.exception.OrganizationNotFoundException;
import com.edu.organization.domain.model.MembershipStatus;
import com.edu.organization.infrastructure.persistence.entity.OrganizationMembershipEntity;
import com.edu.organization.infrastructure.persistence.repository.OrganizationJpaRepository;
import com.edu.organization.infrastructure.persistence.repository.OrganizationMembershipJpaRepository;
import com.edu.shared.kernel.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class MembershipService {

    private final OrganizationJpaRepository organizationRepository;
    private final OrganizationMembershipJpaRepository membershipRepository;
    private final OrganizationAccountQuery accountQuery;

    public MembershipService(
            OrganizationJpaRepository organizationRepository,
            OrganizationMembershipJpaRepository membershipRepository,
            OrganizationAccountQuery accountQuery
    ) {
        this.organizationRepository = organizationRepository;
        this.membershipRepository = membershipRepository;
        this.accountQuery = accountQuery;
    }

    public MembershipView inviteMember(InviteMemberCommand command) {
        if (!organizationRepository.existsById(command.organizationId())) {
            throw new OrganizationNotFoundException(command.organizationId());
        }

        if (!accountQuery.accountExists(command.userId()) || !accountQuery.accountCanJoinOrganization(command.userId())) {
            throw new ResourceNotFoundException("User Account", command.userId().toString());
        }

        if (membershipRepository.existsByOrganizationIdAndUserId(command.organizationId(), command.userId())) {
            throw new MembershipConflictException("User is already a member of this organization");
        }

        OrganizationMembershipEntity entity = new OrganizationMembershipEntity(
                UUID.randomUUID(),
                command.organizationId(),
                command.userId(),
                MembershipStatus.INVITED,
                command.role(),
                null
        );

        OrganizationMembershipEntity saved = membershipRepository.save(entity);
        return toView(saved);
    }

    public MembershipView updateMemberStatus(UpdateMemberStatusCommand command) {
        OrganizationMembershipEntity entity = membershipRepository
                .findByOrganizationIdAndUserId(command.organizationId(), command.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Membership", command.userId().toString()));

        entity.setStatus(command.newStatus());
        if (command.newStatus() == MembershipStatus.ACTIVE && entity.getJoinedAt() == null) {
            entity.setJoinedAt(Instant.now());
        }

        return toView(entity);
    }

    @Transactional(readOnly = true)
    public List<MembershipView> listMembers(UUID organizationId) {
        if (!organizationRepository.existsById(organizationId)) {
            throw new OrganizationNotFoundException(organizationId);
        }
        return membershipRepository.findByOrganizationId(organizationId).stream()
                .map(this::toView)
                .toList();
    }

    private MembershipView toView(OrganizationMembershipEntity entity) {
        return new MembershipView(
                entity.getId(),
                entity.getOrganizationId(),
                entity.getUserId(),
                entity.getRole(),
                entity.getStatus(),
                entity.getJoinedAt()
        );
    }
}
