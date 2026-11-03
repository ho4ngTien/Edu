package com.edu.curriculum.application.service;

import com.edu.curriculum.api.dto.SubjectView;
import com.edu.curriculum.application.command.CreateSubjectCommand;
import com.edu.curriculum.domain.exception.DuplicateSubjectCodeException;
import com.edu.curriculum.domain.model.SubjectStatus;
import com.edu.curriculum.infrastructure.persistence.entity.SubjectEntity;
import com.edu.curriculum.infrastructure.persistence.repository.SubjectJpaRepository;
import com.edu.shared.kernel.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class SubjectService {

    private final SubjectJpaRepository repository;

    public SubjectService(SubjectJpaRepository repository) {
        this.repository = repository;
    }

    public SubjectView createSubject(CreateSubjectCommand command) {
        String trimmedCode = command.code().trim();
        if (repository.existsByCodeAndOrganizationId(trimmedCode, command.organizationId())) {
            throw new DuplicateSubjectCodeException(trimmedCode);
        }

        SubjectEntity entity = new SubjectEntity(
                UUID.randomUUID(),
                trimmedCode,
                command.name().trim(),
                command.description() != null ? command.description().trim() : null,
                command.credits(),
                command.organizationId(),
                SubjectStatus.ACTIVE
        );

        SubjectEntity saved = repository.save(entity);
        return toView(saved);
    }

    @Transactional(readOnly = true)
    public List<SubjectView> listByOrganization(UUID organizationId) {
        return repository.findAllByOrganizationId(organizationId)
                .stream()
                .map(this::toView)
                .toList();
    }

    @Transactional(readOnly = true)
    public SubjectView getById(UUID organizationId, UUID subjectId) {
        return repository.findByIdAndOrganizationId(subjectId, organizationId)
                .map(this::toView)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", subjectId.toString()));
    }

    public SubjectView deprecateSubject(UUID organizationId, UUID subjectId) {
        SubjectEntity entity = repository.findByIdAndOrganizationId(subjectId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", subjectId.toString()));
        entity.setStatus(SubjectStatus.DEPRECATED);
        return toView(entity);
    }

    private SubjectView toView(SubjectEntity entity) {
        return new SubjectView(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                entity.getDescription(),
                entity.getCredits(),
                entity.getOrganizationId(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}

