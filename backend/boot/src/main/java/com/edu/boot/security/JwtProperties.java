package com.edu.boot.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.security.jwt")
@Validated
public record JwtProperties(
        @NotBlank String issuer,
        @NotBlank String audience,
        @NotBlank @Size(min = 44, message = "secret-base64 must represent at least 256 bits") String secretBase64,
        @NotNull Duration accessTokenTtl,
        @NotNull Duration refreshTokenTtl
) {
}
