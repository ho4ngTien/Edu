package com.edu.organization.application.service;

import com.edu.organization.api.dto.MembershipView;
import com.edu.organization.application.command.InviteMemberCommand;
import com.edu.organization.application.command.UpdateMemberStatusCommand;
import com.edu.organization.application.port.OrganizationAccountQuery;
import com.edu.organization.domain.exception.MembershipConflictException;
import com.edu.organization.domain.exception.OrganizationNotFoundException;
import com.edu.organization.domain.model.MembershipStatus;
import com.edu.organization.domain.model.OrganizationRole;
import com.edu.organization.infrastructure.persistence.entity.OrganizationMembershipEntity;
import com.edu.organization.infrastructure.persistence.repository.OrganizationJpaRepository;
import com.edu.organization.infrastructure.persistence.repository.OrganizationMembershipJpaRepository;
import com.edu.shared.kernel.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MembershipServiceTest {

    @Mock
    private OrganizationJpaRepository organizationRepository;

    @Mock
    private OrganizationMembershipJpaRepository membershipRepository;

    @Mock
    private OrganizationAccountQuery accountQuery;

    private MembershipService membershipService;

    @BeforeEach
    void setUp() {
        membershipService = new MembershipService(organizationRepository, membershipRepository, accountQuery);
    }

    @Test
    @DisplayName("AC-2.5.3 & AC-2.5.5: Invite member creates INVITED membership after verifying account via port")
    void inviteMemberSuccess() {
        UUID orgId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        InviteMemberCommand command = new InviteMemberCommand(orgId, userId, OrganizationRole.MEMBER);

        given(organizationRepository.existsById(orgId)).willReturn(true);
        given(accountQuery.accountExists(userId)).willReturn(true);
        given(accountQuery.accountCanJoinOrganization(userId)).willReturn(true);
        given(membershipRepository.existsByOrganizationIdAndUserId(orgId, userId)).willReturn(false);
        given(membershipRepository.save(any(OrganizationMembershipEntity.class))).willAnswer(inv -> inv.getArgument(0));

        MembershipView view = membershipService.inviteMember(command);

        assertThat(view).isNotNull();
        assertThat(view.organizationId()).isEqualTo(orgId);
        assertThat(view.userId()).isEqualTo(userId);
        assertThat(view.role()).isEqualTo(OrganizationRole.MEMBER);
        assertThat(view.status()).isEqualTo(MembershipStatus.INVITED);
        assertThat(view.joinedAt()).isNull();

        ArgumentCaptor<OrganizationMembershipEntity> captor = ArgumentCaptor.forClass(OrganizationMembershipEntity.class);
        verify(membershipRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(MembershipStatus.INVITED);
    }

    @Test
    @DisplayName("Invite member throws OrganizationNotFoundException when org does not exist")
    void inviteMemberOrgNotFound() {
        UUID orgId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        InviteMemberCommand command = new InviteMemberCommand(orgId, userId, OrganizationRole.MEMBER);

        given(organizationRepository.existsById(orgId)).willReturn(false);

        assertThatThrownBy(() -> membershipService.inviteMember(command))
                .isInstanceOf(OrganizationNotFoundException.class);

        verify(membershipRepository, never()).save(any());
    }

    @Test
    @DisplayName("Invite member throws ResourceNotFoundException when user account is not valid")
    void inviteMemberUserAccountInvalid() {
        UUID orgId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        InviteMemberCommand command = new InviteMemberCommand(orgId, userId, OrganizationRole.MEMBER);

        given(organizationRepository.existsById(orgId)).willReturn(true);
        given(accountQuery.accountExists(userId)).willReturn(true);
        given(accountQuery.accountCanJoinOrganization(userId)).willReturn(false);

        assertThatThrownBy(() -> membershipService.inviteMember(command))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User Account");

        verify(membershipRepository, never()).save(any());
    }

    @Test
    @DisplayName("Invite member throws MembershipConflictException when already a member")
    void inviteMemberConflict() {
        UUID orgId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        InviteMemberCommand command = new InviteMemberCommand(orgId, userId, OrganizationRole.MEMBER);

        given(organizationRepository.existsById(orgId)).willReturn(true);
        given(accountQuery.accountExists(userId)).willReturn(true);
        given(accountQuery.accountCanJoinOrganization(userId)).willReturn(true);
        given(membershipRepository.existsByOrganizationIdAndUserId(orgId, userId)).willReturn(true);

        assertThatThrownBy(() -> membershipService.inviteMember(command))
                .isInstanceOf(MembershipConflictException.class);

        verify(membershipRepository, never()).save(any());
    }

    @Test
    @DisplayName("Update member status to ACTIVE sets joinedAt timestamp")
    void updateMemberStatusToActiveSetsJoinedAt() {
        UUID orgId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        OrganizationMembershipEntity entity = new OrganizationMembershipEntity(
                UUID.randomUUID(), orgId, userId, MembershipStatus.INVITED, OrganizationRole.MEMBER, null
        );

        given(membershipRepository.findByOrganizationIdAndUserId(orgId, userId)).willReturn(Optional.of(entity));

        MembershipView view = membershipService.updateMemberStatus(
                new UpdateMemberStatusCommand(orgId, userId, MembershipStatus.ACTIVE)
        );

        assertThat(view.status()).isEqualTo(MembershipStatus.ACTIVE);
        assertThat(view.joinedAt()).isNotNull();
        assertThat(entity.getJoinedAt()).isNotNull();
    }

    @Test
    @DisplayName("List members returns all membership views")
    void listMembers() {
        UUID orgId = UUID.randomUUID();
        OrganizationMembershipEntity m1 = new OrganizationMembershipEntity(
                UUID.randomUUID(), orgId, UUID.randomUUID(), MembershipStatus.ACTIVE, OrganizationRole.OWNER, null
        );
        OrganizationMembershipEntity m2 = new OrganizationMembershipEntity(
                UUID.randomUUID(), orgId, UUID.randomUUID(), MembershipStatus.INVITED, OrganizationRole.MEMBER, null
        );

        given(organizationRepository.existsById(orgId)).willReturn(true);
        given(membershipRepository.findByOrganizationId(orgId)).willReturn(List.of(m1, m2));

        List<MembershipView> result = membershipService.listMembers(orgId);

        assertThat(result).hasSize(2);
    }
}
