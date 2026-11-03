package com.edu.curriculum.api.dto;

import com.edu.curriculum.domain.model.SubjectStatus;

import java.time.Instant;
import java.util.UUID;

public record SubjectView(
        UUID id,
        String code,
        String name,
        String description,
        Integer credits,
        UUID organizationId,
        SubjectStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}

