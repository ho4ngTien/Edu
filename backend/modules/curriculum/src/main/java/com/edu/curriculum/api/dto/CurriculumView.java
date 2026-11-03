package com.edu.curriculum.api.dto;

import java.time.Instant;
import java.util.UUID;

public record CurriculumView(
        UUID id,
        String code,
        String name,
        String description,
        Integer totalCredits,
        UUID organizationId,
        Instant createdAt,
        Instant updatedAt
) {
}

