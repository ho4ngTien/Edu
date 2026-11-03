package com.edu.curriculum.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateSubjectRequest(
        @NotBlank(message = "Code must not be blank")
        String code,

        @NotBlank(message = "Name must not be blank")
        String name,

        String description,

        @NotNull(message = "Credits must not be null")
        @Positive(message = "Credits must be positive")
        Integer credits
) {
}

