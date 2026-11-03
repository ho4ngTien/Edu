package com.edu.academic.api.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record SemesterView(
        UUID id,
        UUID academicYearId,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        UUID organizationId,
        Instant createdAt,
        Instant updatedAt
) {
}

