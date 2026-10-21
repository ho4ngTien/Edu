package com.edu.organization.application.service;

import com.edu.organization.api.dto.OrganizationView;
import com.edu.organization.application.command.CreateOrganizationCommand;
import com.edu.organization.application.command.UpdateOrganizationCommand;
import com.edu.organization.domain.exception.DuplicateOrganizationCodeException;
import com.edu.organization.domain.exception.OrganizationNotFoundException;
import com.edu.organization.domain.model.OrganizationStatus;
import com.edu.organization.infrastructure.persistence.entity.OrganizationEntity;
import com.edu.organization.infrastructure.persistence.repository.OrganizationJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

    @Mock
    private OrganizationJpaRepository organizationRepository;

    private OrganizationService organizationService;

    @BeforeEach
    void setUp() {
        organizationService = new OrganizationService(organizationRepository);
    }

    @Test
    @DisplayName("AC-2.5.3: Create organization creates active organization with uppercase code")
    void createOrganizationSuccess() {
        CreateOrganizationCommand command = new CreateOrganizationCommand("  hust  ", "Hanoi University of Science and Technology");

        given(organizationRepository.existsByCode("HUST")).willReturn(false);
        given(organizationRepository.save(any(OrganizationEntity.class))).willAnswer(inv -> inv.getArgument(0));

        OrganizationView view = organizationService.createOrganization(command);

        assertThat(view).isNotNull();
        assertThat(view.code()).isEqualTo("HUST");
        assertThat(view.name()).isEqualTo("Hanoi University of Science and Technology");
        assertThat(view.status()).isEqualTo(OrganizationStatus.ACTIVE);

        ArgumentCaptor<OrganizationEntity> captor = ArgumentCaptor.forClass(OrganizationEntity.class);
        verify(organizationRepository).save(captor.capture());
        assertThat(captor.getValue().getCode()).isEqualTo("HUST");
    }

    @Test
    @DisplayName("Create organization with duplicate code throws DuplicateOrganizationCodeException")
    void createOrganizationDuplicateCode() {
        CreateOrganizationCommand command = new CreateOrganizationCommand("HUST", "Duplicate University");

        given(organizationRepository.existsByCode("HUST")).willReturn(true);

        assertThatThrownBy(() -> organizationService.createOrganization(command))
                .isInstanceOf(DuplicateOrganizationCodeException.class)
                .hasMessageContaining("HUST");

        verify(organizationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Get organization by ID returns OrganizationView when found")
    void getOrganizationFound() {
        UUID orgId = UUID.randomUUID();
        OrganizationEntity entity = new OrganizationEntity(orgId, "NEU", "National Economics University", OrganizationStatus.ACTIVE);

        given(organizationRepository.findById(orgId)).willReturn(Optional.of(entity));

        OrganizationView view = organizationService.getOrganization(orgId);

        assertThat(view.id()).isEqualTo(orgId);
        assertThat(view.code()).isEqualTo("NEU");
        assertThat(view.name()).isEqualTo("National Economics University");
        assertThat(view.status()).isEqualTo(OrganizationStatus.ACTIVE);
    }

    @Test
    @DisplayName("Get organization by ID throws OrganizationNotFoundException when not found")
    void getOrganizationNotFound() {
        UUID orgId = UUID.randomUUID();
        given(organizationRepository.findById(orgId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> organizationService.getOrganization(orgId))
                .isInstanceOf(OrganizationNotFoundException.class)
                .hasMessageContaining(orgId.toString());
    }

    @Test
    @DisplayName("Update organization modifies name successfully")
    void updateOrganizationSuccess() {
        UUID orgId = UUID.randomUUID();
        OrganizationEntity entity = new OrganizationEntity(orgId, "NEU", "Old Name", OrganizationStatus.ACTIVE);

        given(organizationRepository.findById(orgId)).willReturn(Optional.of(entity));

        OrganizationView view = organizationService.updateOrganization(orgId, new UpdateOrganizationCommand("New Name"));

        assertThat(view.name()).isEqualTo("New Name");
        assertThat(entity.getName()).isEqualTo("New Name");
    }
}
