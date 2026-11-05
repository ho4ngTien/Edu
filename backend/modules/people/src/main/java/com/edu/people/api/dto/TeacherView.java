package com.edu.people.api.dto;

import com.edu.people.domain.model.TeacherStatus;

import java.time.Instant;
import java.util.UUID;

public record TeacherView(
        UUID id,
        UUID userId,
        String teacherCode,
        String fullName,
        String specialization,
        UUID organizationId,
        TeacherStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}

