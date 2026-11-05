package com.edu.curriculum.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateCurriculumRequest(
        @NotBlank(message = "Code must not be blank")
        String code,

        @NotBlank(message = "Name must not be blank")
        String name,

        String description,

        @NotNull(message = "Total credits must not be null")
        @Positive(message = "Total credits must be positive")
        Integer totalCredits
) {
}

