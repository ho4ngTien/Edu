package com.edu.identity.application.service;

import com.edu.identity.api.dto.LoginRequest;
import com.edu.identity.api.dto.LoginResponse;
import com.edu.identity.application.port.TokenIssuer;
import com.edu.identity.domain.model.UserStatus;
import com.edu.identity.infrastructure.persistence.entity.UserEntity;
import com.edu.identity.infrastructure.persistence.repository.UserJpaRepository;
import com.edu.shared.kernel.exception.BadCredentialsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserJpaRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenIssuer tokenIssuer;

    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        authenticationService = new AuthenticationService(userRepository, passwordEncoder, tokenIssuer);
    }

    @Test
    @DisplayName("AC-2.6.3.1: Login success with valid username and password returns access and refresh tokens")
    void loginSuccessByUsername() {
        UUID userId = UUID.randomUUID();
        UserEntity user = new UserEntity(userId, "teacher1", "teacher@edu.vn", "$2a$10$encodedPassword", UserStatus.ACTIVE);

        given(userRepository.findByUsername("teacher1")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("SecretPass123!", "$2a$10$encodedPassword")).willReturn(true);
        given(tokenIssuer.issue(userId)).willReturn(new TokenIssuer.IssuedTokens("jwt-access-token", "raw-refresh-token", 900L));

        LoginResponse response = authenticationService.login(new LoginRequest("teacher1", "SecretPass123!"));

        assertThat(response.accessToken()).isEqualTo("jwt-access-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(900L);
        assertThat(response.refreshToken()).isEqualTo("raw-refresh-token");
    }

    @Test
    @DisplayName("AC-2.6.3.1: Login success with valid email")
    void loginSuccessByEmail() {
        UUID userId = UUID.randomUUID();
        UserEntity user = new UserEntity(userId, "teacher1", "teacher@edu.vn", "$2a$10$encodedPassword", UserStatus.ACTIVE);

        given(userRepository.findByUsername("teacher@edu.vn")).willReturn(Optional.empty());
        given(userRepository.findByEmail("teacher@edu.vn")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("SecretPass123!", "$2a$10$encodedPassword")).willReturn(true);
        given(tokenIssuer.issue(userId)).willReturn(new TokenIssuer.IssuedTokens("jwt-access-token", "raw-refresh-token", 900L));

        LoginResponse response = authenticationService.login(new LoginRequest("teacher@edu.vn", "SecretPass123!"));

        assertThat(response.accessToken()).isEqualTo("jwt-access-token");
        assertThat(response.refreshToken()).isEqualTo("raw-refresh-token");
    }

    @Test
    @DisplayName("AC-2.6.3.2: Login fails with wrong password throws BadCredentialsException")
    void loginFailsWrongPassword() {
        UUID userId = UUID.randomUUID();
        UserEntity user = new UserEntity(userId, "teacher1", "teacher@edu.vn", "$2a$10$encodedPassword", UserStatus.ACTIVE);

        given(userRepository.findByUsername("teacher1")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("WrongPassword", "$2a$10$encodedPassword")).willReturn(false);

        assertThatThrownBy(() -> authenticationService.login(new LoginRequest("teacher1", "WrongPassword")))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid username/email or password");
    }

    @Test
    @DisplayName("AC-2.6.3.3: Login fails when user not found, executes dummy check to prevent timing attack")
    void loginFailsUserNotFound() {
        given(userRepository.findByUsername("unknown")).willReturn(Optional.empty());
        given(userRepository.findByEmail("unknown")).willReturn(Optional.empty());

        assertThatThrownBy(() -> authenticationService.login(new LoginRequest("unknown", "Password123!")))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid username/email or password");

        verify(passwordEncoder).matches(eq("Password123!"), any(String.class));
    }

    @Test
    @DisplayName("AC-2.6.3.4: Login fails when account is not ACTIVE")
    void loginFailsSuspendedAccount() {
        UUID userId = UUID.randomUUID();
        UserEntity user = new UserEntity(userId, "suspended_user", "suspended@edu.vn", "$2a$10$encodedPassword", UserStatus.DISABLED);

        given(userRepository.findByUsername("suspended_user")).willReturn(Optional.of(user));

        assertThatThrownBy(() -> authenticationService.login(new LoginRequest("suspended_user", "Password123!")))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Account is disabled or suspended");
    }
}
