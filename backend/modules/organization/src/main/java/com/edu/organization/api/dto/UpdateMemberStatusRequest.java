package com.edu.organization.api.dto;

import com.edu.organization.domain.model.MembershipStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateMemberStatusRequest(
        @NotNull(message = "Status must not be null")
        MembershipStatus status
) {}
