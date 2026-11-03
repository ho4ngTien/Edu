package com.edu.people.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateTeacherRequest(
        @NotNull(message = "User ID must not be null")
        UUID userId,

        @NotBlank(message = "Teacher code must not be blank")
        String teacherCode,

        @NotBlank(message = "Full name must not be blank")
        String fullName,

        String specialization
) {
}

