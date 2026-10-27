package com.edu.boot.security;

import com.edu.identity.application.port.TokenIssuer;
import com.edu.identity.application.service.RefreshTokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class TokenIssuerAdapterTest {

    @Mock
    private JwtAccessTokenService jwtAccessTokenService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Test
    @DisplayName("issue delegates to JwtAccessTokenService and RefreshTokenService")
    void issueSuccess() {
        JwtProperties properties = new JwtProperties(
                "edu-platform",
                "edu-api",
                "secret-key-at-least-256-bits-long-edu-monolith-jwt-signing-key",
                Duration.ofMinutes(15),
                Duration.ofDays(7)
        );

        TokenIssuerAdapter adapter = new TokenIssuerAdapter(jwtAccessTokenService, refreshTokenService, properties);

        UUID userId = UUID.randomUUID();
        given(jwtAccessTokenService.generateAccessToken(userId)).willReturn("jwt-token-123");
        given(refreshTokenService.createRefreshToken(userId, null, Duration.ofDays(7))).willReturn("refresh-token-456");

        TokenIssuer.IssuedTokens tokens = adapter.issue(userId);

        assertThat(tokens.accessToken()).isEqualTo("jwt-token-123");
        assertThat(tokens.refreshToken()).isEqualTo("refresh-token-456");
        assertThat(tokens.expiresIn()).isEqualTo(900L);
    }
}
