package com.edu.academic.application.command;

import java.util.UUID;

public record CreateClassRoomCommand(
        String code,
        String name,
        UUID organizationId,
        UUID academicYearId,
        Integer capacity
) {
}

