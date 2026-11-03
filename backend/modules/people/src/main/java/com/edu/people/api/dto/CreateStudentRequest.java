package com.edu.people.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record CreateStudentRequest(
        @NotNull(message = "User ID must not be null")
        UUID userId,

        @NotBlank(message = "Student code must not be blank")
        String studentCode,

        @NotBlank(message = "Full name must not be blank")
        String fullName,

        LocalDate dateOfBirth,

        Instant enrolledAt
) {
}

