package com.edu.organization.application.service;

import com.edu.organization.api.dto.OrganizationView;
import com.edu.organization.application.command.CreateOrganizationCommand;
import com.edu.organization.application.command.UpdateOrganizationCommand;
import com.edu.organization.domain.exception.DuplicateOrganizationCodeException;
import com.edu.organization.domain.exception.OrganizationNotFoundException;
import com.edu.organization.domain.model.OrganizationStatus;
import com.edu.organization.infrastructure.persistence.entity.OrganizationEntity;
import com.edu.organization.infrastructure.persistence.repository.OrganizationJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class OrganizationService {

    private final OrganizationJpaRepository organizationRepository;

    public OrganizationService(OrganizationJpaRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    public OrganizationView createOrganization(CreateOrganizationCommand command) {
        String trimmedCode = command.code().trim().toUpperCase();
        if (organizationRepository.existsByCode(trimmedCode)) {
            throw new DuplicateOrganizationCodeException(trimmedCode);
        }

        OrganizationEntity entity = new OrganizationEntity(
                UUID.randomUUID(),
                trimmedCode,
                command.name().trim(),
                OrganizationStatus.ACTIVE
        );

        OrganizationEntity saved = organizationRepository.save(entity);
        return toView(saved);
    }

    @Transactional(readOnly = true)
    public OrganizationView getOrganization(UUID organizationId) {
        return organizationRepository.findById(organizationId)
                .map(this::toView)
                .orElseThrow(() -> new OrganizationNotFoundException(organizationId));
    }

    public OrganizationView updateOrganization(UUID organizationId, UpdateOrganizationCommand command) {
        OrganizationEntity entity = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new OrganizationNotFoundException(organizationId));

        entity.setName(command.name().trim());
        return toView(entity);
    }

    public OrganizationView updateOrganizationStatus(UUID organizationId, OrganizationStatus status) {
        OrganizationEntity entity = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new OrganizationNotFoundException(organizationId));

        entity.setStatus(status);
        return toView(entity);
    }

    private OrganizationView toView(OrganizationEntity entity) {
        return new OrganizationView(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                entity.getStatus(),
                entity.getCreatedAt()
        );
    }
}
