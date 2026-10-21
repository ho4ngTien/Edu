package com.edu.organization.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateOrganizationRequest(
        @NotBlank(message = "Organization name must not be blank")
        @Size(max = 255, message = "Organization name cannot exceed 255 characters")
        String name
) {}
