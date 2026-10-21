package com.edu.organization.api.dto;

import com.edu.organization.domain.model.OrganizationStatus;

import java.time.Instant;
import java.util.UUID;

public record OrganizationView(
        UUID id,
        String code,
        String name,
        OrganizationStatus status,
        Instant createdAt
) {}
