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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class IdentityService {

    private final UserJpaRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public IdentityService(UserJpaRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AccountView registerAccount(RegisterAccountCommand command) {
        String trimmedUsername = command.username().trim().toLowerCase();
        String trimmedEmail = command.email().trim().toLowerCase();

        if (userRepository.existsByUsername(trimmedUsername)) {
            throw new DuplicateUsernameException(trimmedUsername);
        }
        if (userRepository.existsByEmail(trimmedEmail)) {
            throw new DuplicateEmailException(trimmedEmail);
        }

        String encodedPassword = passwordEncoder.encode(command.rawPassword());

        UserEntity entity = new UserEntity(
                UUID.randomUUID(),
                trimmedUsername,
                trimmedEmail,
                encodedPassword,
                UserStatus.ACTIVE
        );

        UserEntity saved = userRepository.save(entity);
        return toView(saved);
    }

    @Transactional(readOnly = true)
    public AccountView getAccount(UUID userId) {
        return userRepository.findById(userId)
                .map(this::toView)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));
    }

    public AccountView changeAccountStatus(ChangeAccountStatusCommand command) {
        UserEntity entity = userRepository.findById(command.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User", command.userId().toString()));

        entity.setStatus(command.status());
        return toView(entity);
    }

    private AccountView toView(UserEntity entity) {
        return new AccountView(
                entity.getId(),
                entity.getUsername(),
                entity.getEmail(),
                entity.getStatus()
        );
    }
}
