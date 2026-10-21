package com.edu.identity.application.port;

import com.edu.identity.domain.model.UserStatus;
import com.edu.identity.infrastructure.persistence.entity.UserEntity;
import com.edu.identity.infrastructure.persistence.repository.UserJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class IdentityAccountQueryAdapterTest {

    @Mock
    private UserJpaRepository userRepository;

    private IdentityAccountQueryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new IdentityAccountQueryAdapter(userRepository);
    }

    @Test
    @DisplayName("AC-2.5.5: Port adapter verifies account existence")
    void accountExists() {
        UUID userId = UUID.randomUUID();
        given(userRepository.existsById(userId)).willReturn(true);

        assertThat(adapter.accountExists(userId)).isTrue();
    }

    @Test
    @DisplayName("AC-2.5.5: Port adapter verifies if account can join organization (ACTIVE status only)")
    void accountCanJoinOrganization() {
        UUID activeUserId = UUID.randomUUID();
        UUID lockedUserId = UUID.randomUUID();
        UUID notFoundUserId = UUID.randomUUID();

        UserEntity activeUser = new UserEntity(activeUserId, "active", "active@edu.vn", "hash", UserStatus.ACTIVE);
        UserEntity lockedUser = new UserEntity(lockedUserId, "locked", "locked@edu.vn", "hash", UserStatus.LOCKED);

        given(userRepository.findById(activeUserId)).willReturn(Optional.of(activeUser));
        given(userRepository.findById(lockedUserId)).willReturn(Optional.of(lockedUser));
        given(userRepository.findById(notFoundUserId)).willReturn(Optional.empty());

        assertThat(adapter.accountCanJoinOrganization(activeUserId)).isTrue();
        assertThat(adapter.accountCanJoinOrganization(lockedUserId)).isFalse();
        assertThat(adapter.accountCanJoinOrganization(notFoundUserId)).isFalse();
    }
}
