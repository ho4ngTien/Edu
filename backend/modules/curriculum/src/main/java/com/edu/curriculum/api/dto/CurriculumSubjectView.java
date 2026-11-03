package com.edu.curriculum.api.dto;

import java.time.Instant;
import java.util.UUID;

public record CurriculumSubjectView(
        UUID curriculumId,
        UUID subjectId,
        Boolean isRequired,
        Integer sequenceOrder,
        Instant assignedAt
) {
}

