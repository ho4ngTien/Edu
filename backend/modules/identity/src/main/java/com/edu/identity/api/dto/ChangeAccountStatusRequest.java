package com.edu.identity.api.dto;

import com.edu.identity.domain.model.UserStatus;
import jakarta.validation.constraints.NotNull;

public record ChangeAccountStatusRequest(
        @NotNull(message = "Status must not be null")
        UserStatus status
) {}
