package com.edu.academic.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateSemesterRequest(
        @NotBlank(message = "Name must not be blank")
        String name,

        @NotNull(message = "Start date must not be null")
        LocalDate startDate,

        @NotNull(message = "End date must not be null")
        LocalDate endDate
) {
}

