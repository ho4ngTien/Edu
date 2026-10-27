package com.edu.identity.application.port;

import java.util.UUID;

public interface TokenIssuer {

    IssuedTokens issue(UUID userId);

    record IssuedTokens(
            String accessToken,
            String refreshToken,
            long expiresIn
    ) {
    }
}
