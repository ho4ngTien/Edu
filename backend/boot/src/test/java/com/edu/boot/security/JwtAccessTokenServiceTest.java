package com.edu.boot.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class JwtAccessTokenServiceTest {

    @Mock
    private JwtEncoder jwtEncoder;

    @Test
    @DisplayName("generateAccessToken builds valid claims with 15min TTL and encodes JWT")
    void generateAccessTokenSuccess() {
        Instant fixedInstant = Instant.parse("2026-10-27T10:00:00Z");
        Clock clock = Clock.fixed(fixedInstant, ZoneOffset.UTC);
        JwtProperties properties = new JwtProperties(
                "edu-platform",
                "edu-api",
                "secret-key-at-least-256-bits-long-edu-monolith-jwt-signing-key",
                Duration.ofMinutes(15),
                Duration.ofDays(7)
        );

        JwtAccessTokenService service = new JwtAccessTokenService(jwtEncoder, properties, clock);

        UUID userId = UUID.randomUUID();
        Jwt mockJwt = org.mockito.Mockito.mock(Jwt.class);
        given(mockJwt.getTokenValue()).willReturn("mock-jwt-token-value");
        given(jwtEncoder.encode(any(JwtEncoderParameters.class))).willReturn(mockJwt);

        String token = service.generateAccessToken(userId);

        assertThat(token).isEqualTo("mock-jwt-token-value");

        ArgumentCaptor<JwtEncoderParameters> captor = ArgumentCaptor.forClass(JwtEncoderParameters.class);
        verify(jwtEncoder).encode(captor.capture());

        JwtEncoderParameters params = captor.getValue();
        assertThat(params.getClaims().getSubject()).isEqualTo(userId.toString());
        assertThat(params.getClaims().getClaimAsString("iss")).isEqualTo("edu-platform");
        assertThat(params.getClaims().getAudience()).containsExactly("edu-api");
        assertThat(params.getClaims().getIssuedAt()).isEqualTo(fixedInstant);
        assertThat(params.getClaims().getExpiresAt()).isEqualTo(fixedInstant.plus(Duration.ofMinutes(15)));
    }
}
