package com.edu.curriculum.application.command;

import java.util.UUID;

public record AssignSubjectCommand(
        UUID curriculumId,
        UUID subjectId,
        UUID organizationId,
        Boolean isRequired,
        Integer sequenceOrder
) {
}

