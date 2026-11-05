package com.edu.people.application.command;

import java.util.UUID;

public record CreateTeacherCommand(
        UUID userId,
        String teacherCode,
        String fullName,
        String specialization,
        UUID organizationId
) {
}

