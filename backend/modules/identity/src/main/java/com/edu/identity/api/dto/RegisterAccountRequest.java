package com.edu.identity.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterAccountRequest(
        @NotBlank(message = "Username must not be blank")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username can only contain letters, numbers, dots, and hyphens")
        String username,

        @NotBlank(message = "Email must not be blank")
        @Email(message = "Email must be a valid email address")
        @Size(max = 255)
        String email,

        @NotBlank(message = "Password must not be blank")
        @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters")
        String password
) {}
