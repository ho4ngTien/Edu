# Phase 2.5: Identity Application Services

> Thuộc [Phase 2.5: Identity & Organization Core](README.md)

### 4.4. Identity Application Services

#### 1. Triển khai `IdentityService`

```java
package com.edu.identity.application.service;

import com.edu.identity.application.command.ChangeAccountStatusCommand;
import com.edu.identity.application.command.RegisterAccountCommand;
import com.edu.identity.api.dto.AccountView;
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
        if (userRepository.existsByUsername(command.username())) {
            throw new DuplicateUsernameException(command.username());
        }
        if (userRepository.existsByEmail(command.email())) {
            throw new DuplicateEmailException(command.email());
        }

        String encodedPassword = passwordEncoder.encode(command.rawPassword());

        UserEntity entity = new UserEntity(
                UUID.randomUUID(),
                command.username().trim().toLowerCase(),
                command.email().trim().toLowerCase(),
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
```

#### 2. Adapter triển khai `OrganizationAccountQuery` trong module `identity`

```java
package com.edu.identity.application.port;

import com.edu.identity.domain.model.UserStatus;
import com.edu.identity.infrastructure.persistence.repository.UserJpaRepository;
import com.edu.organization.application.port.OrganizationAccountQuery;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class IdentityAccountQueryAdapter implements OrganizationAccountQuery {

    private final UserJpaRepository userRepository;

    public IdentityAccountQueryAdapter(UserJpaRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean accountExists(UUID userId) {
        return userRepository.existsById(userId);
    }

    @Override
    public boolean accountCanJoinOrganization(UUID userId) {
        return userRepository.findById(userId)
                .map(u -> u.getStatus() == UserStatus.ACTIVE)
                .orElse(false);
    }
}
```

---
