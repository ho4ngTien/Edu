package com.edu.organization.application.command;

import com.edu.organization.domain.model.MembershipStatus;

import java.util.UUID;

public record UpdateMemberStatusCommand(
        UUID organizationId,
        UUID userId,
        MembershipStatus newStatus
) {}
