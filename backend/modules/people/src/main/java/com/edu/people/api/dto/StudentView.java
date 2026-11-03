package com.edu.people.api.dto;

import com.edu.people.domain.model.StudentStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record StudentView(
        UUID id,
        UUID userId,
        String studentCode,
        String fullName,
        LocalDate dateOfBirth,
        UUID organizationId,
        StudentStatus status,
        Instant enrolledAt,
        Instant createdAt,
        Instant updatedAt
) {
}

