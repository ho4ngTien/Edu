package com.edu.organization.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateOrganizationRequest(
        @NotBlank(message = "Organization code must not be blank")
        @Size(min = 2, max = 50, message = "Organization code must be between 2 and 50 characters")
        @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "Organization code can only contain alphanumeric characters, underscores, and hyphens")
        String code,

        @NotBlank(message = "Organization name must not be blank")
        @Size(max = 255, message = "Organization name cannot exceed 255 characters")
        String name
) {}
