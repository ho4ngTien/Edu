package com.edu.academic.api.dto;

import java.time.Instant;
import java.util.UUID;

public record ClassRoomView(
        UUID id,
        String code,
        String name,
        UUID organizationId,
        UUID academicYearId,
        Integer capacity,
        Instant createdAt,
        Instant updatedAt
) {
}

