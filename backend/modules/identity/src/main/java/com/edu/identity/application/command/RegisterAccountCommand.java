package com.edu.identity.application.command;

public record RegisterAccountCommand(
        String username,
        String email,
        String rawPassword
) {}
