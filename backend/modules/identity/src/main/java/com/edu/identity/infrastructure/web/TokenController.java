package com.edu.identity.infrastructure.web;

import com.edu.identity.api.dto.LoginResponse;
import com.edu.identity.api.dto.LogoutRequest;
import com.edu.identity.api.dto.RefreshTokenRequest;
import com.edu.identity.application.port.TokenIssuer;
import com.edu.identity.application.service.RefreshTokenService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/api/v1/auth")
public class TokenController {

    private final RefreshTokenService refreshTokenService;
    private final TokenIssuer tokenIssuer;

    public TokenController(RefreshTokenService refreshTokenService, TokenIssuer tokenIssuer) {
        this.refreshTokenService = refreshTokenService;
        this.tokenIssuer = tokenIssuer;
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        var rotation = refreshTokenService.rotateRefreshToken(request.refreshToken(), Duration.ofDays(7));
        var newTokens = tokenIssuer.issue(rotation.userId());

        return ResponseEntity.ok(new LoginResponse(
                newTokens.accessToken(),
                "Bearer",
                newTokens.expiresIn(),
                rotation.newRefreshToken()
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest request) {
        refreshTokenService.logout(request.refreshToken());
        return ResponseEntity.noContent().build();
    }
}
