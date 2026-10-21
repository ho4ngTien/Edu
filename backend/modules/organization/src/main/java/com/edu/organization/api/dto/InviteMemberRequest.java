package com.edu.organization.api.dto;

import com.edu.organization.domain.model.OrganizationRole;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record InviteMemberRequest(
        @NotNull(message = "User ID must not be null")
        UUID userId,

        @NotNull(message = "Role must not be null")
        OrganizationRole role
) {}
