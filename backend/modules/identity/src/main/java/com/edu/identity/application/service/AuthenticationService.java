package com.edu.identity.application.service;

import com.edu.identity.api.dto.LoginRequest;
import com.edu.identity.api.dto.LoginResponse;
import com.edu.identity.application.port.TokenIssuer;
import com.edu.identity.domain.model.UserStatus;
import com.edu.identity.infrastructure.persistence.entity.UserEntity;
import com.edu.identity.infrastructure.persistence.repository.UserJpaRepository;
import com.edu.shared.kernel.exception.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthenticationService {

    private static final String DUMMY_HASH = "$2a$10$wT3wY2K7R8mQ5nL9pX4rOe8uI1vY6sT9aB2cD5eF8gH1jK4mN7pQ.";

    private final UserJpaRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenIssuer tokenIssuer;

    public AuthenticationService(
            UserJpaRepository userRepository,
            PasswordEncoder passwordEncoder,
            TokenIssuer tokenIssuer
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenIssuer = tokenIssuer;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Optional<UserEntity> userOpt = userRepository.findByUsername(request.usernameOrEmail());
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByEmail(request.usernameOrEmail());
        }

        if (userOpt.isEmpty()) {
            passwordEncoder.matches(request.password(), DUMMY_HASH);
            throw new BadCredentialsException("Invalid username/email or password");
        }

        UserEntity user = userOpt.get();

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BadCredentialsException("Account is disabled or suspended");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid username/email or password");
        }

        TokenIssuer.IssuedTokens tokens = tokenIssuer.issue(user.getId());

        return new LoginResponse(
                tokens.accessToken(),
                "Bearer",
                tokens.expiresIn(),
                tokens.refreshToken()
        );
    }
}
