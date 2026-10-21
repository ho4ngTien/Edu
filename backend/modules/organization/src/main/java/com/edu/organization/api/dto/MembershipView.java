package com.edu.organization.api.dto;

import com.edu.organization.domain.model.MembershipStatus;
import com.edu.organization.domain.model.OrganizationRole;

import java.time.Instant;
import java.util.UUID;

public record MembershipView(
        UUID id,
        UUID organizationId,
        UUID userId,
        OrganizationRole role,
        MembershipStatus status,
        Instant joinedAt
) {}
