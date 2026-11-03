package com.edu.curriculum.api.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AssignSubjectRequest(
        @NotNull(message = "Subject ID must not be null")
        UUID subjectId,

        Boolean isRequired,

        Integer sequenceOrder
) {
}

