package com.edu.academic.application.command;

import com.edu.academic.domain.model.AcademicStatus;

import java.time.LocalDate;
import java.util.UUID;

public record CreateAcademicYearCommand(
        String name,
        LocalDate startDate,
        LocalDate endDate,
        UUID organizationId,
        AcademicStatus status
) {
}

