package com.edu.curriculum.application.command;

import java.util.UUID;

public record CreateSubjectCommand(
        String code,
        String name,
        String description,
        Integer credits,
        UUID organizationId
) {
}

