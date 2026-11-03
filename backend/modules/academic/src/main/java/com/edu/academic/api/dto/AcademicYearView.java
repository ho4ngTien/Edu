package com.edu.academic.api.dto;

import com.edu.academic.domain.model.AcademicStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record AcademicYearView(
        UUID id,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        UUID organizationId,
        AcademicStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}

