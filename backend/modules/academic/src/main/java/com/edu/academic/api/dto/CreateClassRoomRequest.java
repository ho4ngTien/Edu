package com.edu.academic.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record CreateClassRoomRequest(
        @NotBlank(message = "Code must not be blank")
        String code,

        @NotBlank(message = "Name must not be blank")
        String name,

        @NotNull(message = "Academic year ID must not be null")
        UUID academicYearId,

        @Positive(message = "Capacity must be positive")
        Integer capacity
) {
}

