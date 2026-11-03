package com.edu.academic.application.command;

import java.time.LocalDate;
import java.util.UUID;

public record CreateSemesterCommand(
        UUID academicYearId,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        UUID organizationId
) {
}

