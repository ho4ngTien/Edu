package com.edu.identity.application.service;

import com.edu.identity.api.dto.AccountView;
import com.edu.identity.application.command.ChangeAccountStatusCommand;
import com.edu.identity.application.command.RegisterAccountCommand;
import com.edu.identity.domain.exception.DuplicateEmailException;
import com.edu.identity.domain.exception.DuplicateUsernameException;
import com.edu.identity.domain.model.UserStatus;
import com.edu.identity.infrastructure.persistence.entity.UserEntity;
import com.edu.identity.infrastructure.persistence.repository.UserJpaRepository;
import com.edu.shared.kernel.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class IdentityServiceTest {

    @Mock
    private UserJpaRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private IdentityService identityService;

    @BeforeEach
    void setUp() {
        identityService = new IdentityService(userRepository, passwordEncoder);
    }

    @Test
    @DisplayName("AC-2.5.4: Register account encodes password and saves user with ACTIVE status")
    void registerAccountSuccess() {
        RegisterAccountCommand command = new RegisterAccountCommand(
                "  JohnDoe  ",
                "  John@Example.com  ",
                "SecretP@ssword123"
        );

        given(userRepository.existsByUsername("johndoe")).willReturn(false);
        given(userRepository.existsByEmail("john@example.com")).willReturn(false);
        given(passwordEncoder.encode("SecretP@ssword123")).willReturn("$2a$10$hashedPasswordValue");
        given(userRepository.save(any(UserEntity.class))).willAnswer(invocation -> invocation.getArgument(0));

        AccountView result = identityService.registerAccount(command);

        assertThat(result).isNotNull();
        assertThat(result.id()).isNotNull();
        assertThat(result.username()).isEqualTo("johndoe");
        assertThat(result.email()).isEqualTo("john@example.com");
        assertThat(result.status()).isEqualTo(UserStatus.ACTIVE);

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());
        UserEntity saved = captor.getValue();
        assertThat(saved.getPasswordHash()).isEqualTo("$2a$10$hashedPasswordValue");
    }

    @Test
    @DisplayName("Register account with duplicate username throws DuplicateUsernameException")
    void registerAccountDuplicateUsername() {
        RegisterAccountCommand command = new RegisterAccountCommand(
                "johndoe",
                "john@example.com",
                "SecretP@ssword123"
        );

        given(userRepository.existsByUsername("johndoe")).willReturn(true);

        assertThatThrownBy(() -> identityService.registerAccount(command))
                .isInstanceOf(DuplicateUsernameException.class)
                .hasMessageContaining("johndoe");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Register account with duplicate email throws DuplicateEmailException")
    void registerAccountDuplicateEmail() {
        RegisterAccountCommand command = new RegisterAccountCommand(
                "johndoe",
                "john@example.com",
                "SecretP@ssword123"
        );

        given(userRepository.existsByUsername("johndoe")).willReturn(false);
        given(userRepository.existsByEmail("john@example.com")).willReturn(true);

        assertThatThrownBy(() -> identityService.registerAccount(command))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("john@example.com");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Get account by ID returns AccountView when found")
    void getAccountFound() {
        UUID userId = UUID.randomUUID();
        UserEntity entity = new UserEntity(userId, "johndoe", "john@example.com", "hash", UserStatus.ACTIVE);

        given(userRepository.findById(userId)).willReturn(Optional.of(entity));

        AccountView result = identityService.getAccount(userId);

        assertThat(result.id()).isEqualTo(userId);
        assertThat(result.username()).isEqualTo("johndoe");
        assertThat(result.email()).isEqualTo("john@example.com");
        assertThat(result.status()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("Get account by ID throws ResourceNotFoundException when not found")
    void getAccountNotFound() {
        UUID userId = UUID.randomUUID();
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> identityService.getAccount(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(userId.toString());
    }

    @Test
    @DisplayName("Change account status updates status successfully")
    void changeAccountStatusSuccess() {
        UUID userId = UUID.randomUUID();
        UserEntity entity = new UserEntity(userId, "johndoe", "john@example.com", "hash", UserStatus.ACTIVE);

        given(userRepository.findById(userId)).willReturn(Optional.of(entity));

        AccountView result = identityService.changeAccountStatus(new ChangeAccountStatusCommand(userId, UserStatus.LOCKED));

        assertThat(result.status()).isEqualTo(UserStatus.LOCKED);
        assertThat(entity.getStatus()).isEqualTo(UserStatus.LOCKED);
    }
}
