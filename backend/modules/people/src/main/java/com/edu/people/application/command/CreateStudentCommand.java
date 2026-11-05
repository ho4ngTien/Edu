package com.edu.people.application.command;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record CreateStudentCommand(
        UUID userId,
        String studentCode,
        String fullName,
        LocalDate dateOfBirth,
        UUID organizationId,
        Instant enrolledAt
) {
}

