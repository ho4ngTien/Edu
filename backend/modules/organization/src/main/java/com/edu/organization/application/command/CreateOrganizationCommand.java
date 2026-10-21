package com.edu.organization.application.command;

public record CreateOrganizationCommand(
        String code,
        String name
) {}
