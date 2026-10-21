package com.edu.identity.application.command;

import com.edu.identity.domain.model.UserStatus;

import java.util.UUID;

public record ChangeAccountStatusCommand(
        UUID userId,
        UserStatus status
) {}
