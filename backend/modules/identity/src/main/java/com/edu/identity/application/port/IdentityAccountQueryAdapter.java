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
                .map(user -> user.getStatus() == UserStatus.ACTIVE)
                .orElse(false);
    }
}
