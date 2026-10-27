package com.edu.identity.infrastructure.web;

import com.edu.identity.application.port.TokenIssuer;
import com.edu.identity.application.service.RefreshTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Duration;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TokenControllerTest {

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private TokenIssuer tokenIssuer;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TokenController(refreshTokenService, tokenIssuer))
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/auth/refresh rotates token and returns 200 OK")
    void refreshSuccess() throws Exception {
        UUID userId = UUID.randomUUID();
        given(refreshTokenService.rotateRefreshToken(eq("valid-refresh-token"), any(Duration.class)))
                .willReturn(new RefreshTokenService.RotationResult(userId, "new-refresh-token"));

        given(tokenIssuer.issue(userId))
                .willReturn(new TokenIssuer.IssuedTokens("new-access-token", "unused-here", 900L));

        String json = """
                {
                    "refreshToken": "valid-refresh-token"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(900))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/logout revokes token and returns 204 No Content")
    void logoutSuccess() throws Exception {
        String json = """
                {
                    "refreshToken": "logout-token"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNoContent());

        verify(refreshTokenService).logout("logout-token");
    }
}
