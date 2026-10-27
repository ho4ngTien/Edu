package com.edu.boot.security;

import com.edu.identity.application.port.TokenIssuer;
import com.edu.identity.application.service.RefreshTokenService;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TokenIssuerAdapter implements TokenIssuer {

    private final JwtAccessTokenService jwtAccessTokenService;
    private final RefreshTokenService refreshTokenService;
    private final JwtProperties jwtProperties;

    public TokenIssuerAdapter(
            JwtAccessTokenService jwtAccessTokenService,
            RefreshTokenService refreshTokenService,
            JwtProperties jwtProperties
    ) {
        this.jwtAccessTokenService = jwtAccessTokenService;
        this.refreshTokenService = refreshTokenService;
        this.jwtProperties = jwtProperties;
    }

    @Override
    public IssuedTokens issue(UUID userId) {
        String accessToken = jwtAccessTokenService.generateAccessToken(userId);
        String refreshToken = refreshTokenService.createRefreshToken(userId, null, jwtProperties.refreshTokenTtl());
        long expiresIn = jwtProperties.accessTokenTtl().toSeconds();

        return new IssuedTokens(accessToken, refreshToken, expiresIn);
    }
}
