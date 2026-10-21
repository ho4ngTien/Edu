package com.edu.identity.api.dto;

import com.edu.identity.domain.model.UserStatus;

import java.util.UUID;

public record AccountView(
        UUID id,
        String username,
        String email,
        UserStatus status
) {}
