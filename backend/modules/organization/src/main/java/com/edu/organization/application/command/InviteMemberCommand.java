package com.edu.organization.application.command;

import com.edu.organization.domain.model.OrganizationRole;

import java.util.UUID;

public record InviteMemberCommand(
        UUID organizationId,
        UUID userId,
        OrganizationRole role
) {}
