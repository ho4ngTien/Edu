package com.edu.curriculum.application.command;

import java.util.UUID;

public record CreateCurriculumCommand(
        String code,
        String name,
        String description,
        Integer totalCredits,
        UUID organizationId
) {
}

