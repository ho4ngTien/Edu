package com.edu.academic.application.service;

import com.edu.academic.api.dto.AcademicYearView;
import com.edu.academic.application.command.CreateAcademicYearCommand;
import com.edu.academic.domain.exception.InvalidAcademicPeriodException;
import com.edu.academic.domain.model.AcademicStatus;
import com.edu.academic.infrastructure.persistence.entity.AcademicYearEntity;
import com.edu.academic.infrastructure.persistence.repository.AcademicYearJpaRepository;
import com.edu.shared.kernel.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AcademicYearService {

    private final AcademicYearJpaRepository repository;

    public AcademicYearService(AcademicYearJpaRepository repository) {
        this.repository = repository;
    }

    public AcademicYearView createAcademicYear(CreateAcademicYearCommand command) {
        if (!command.endDate().isAfter(command.startDate())) {
            throw new InvalidAcademicPeriodException("End date must be after start date");
        }

        AcademicStatus status = command.status() != null ? command.status() : AcademicStatus.UPCOMING;

        AcademicYearEntity entity = new AcademicYearEntity(
                UUID.randomUUID(),
                command.name().trim(),
                command.startDate(),
                command.endDate(),
                command.organizationId(),
                status
        );

        AcademicYearEntity saved = repository.save(entity);
        return toView(saved);
    }

    @Transactional(readOnly = true)
    public List<AcademicYearView> listByOrganization(UUID organizationId) {
        return repository.findAllByOrganizationId(organizationId)
                .stream()
                .map(this::toView)
                .toList();
    }

    @Transactional(readOnly = true)
    public AcademicYearView getById(UUID organizationId, UUID id) {
        return repository.findByIdAndOrganizationId(id, organizationId)
                .map(this::toView)
                .orElseThrow(() -> new ResourceNotFoundException("AcademicYear", id.toString()));
    }

    private AcademicYearView toView(AcademicYearEntity entity) {
        return new AcademicYearView(
                entity.getId(),
                entity.getName(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getOrganizationId(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}

