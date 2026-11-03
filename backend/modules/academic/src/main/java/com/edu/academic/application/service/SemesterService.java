package com.edu.academic.application.service;

import com.edu.academic.api.dto.SemesterView;
import com.edu.academic.application.command.CreateSemesterCommand;
import com.edu.academic.domain.exception.InvalidAcademicPeriodException;
import com.edu.academic.infrastructure.persistence.entity.SemesterEntity;
import com.edu.academic.infrastructure.persistence.repository.AcademicYearJpaRepository;
import com.edu.academic.infrastructure.persistence.repository.SemesterJpaRepository;
import com.edu.shared.kernel.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class SemesterService {

    private final SemesterJpaRepository semesterRepository;
    private final AcademicYearJpaRepository academicYearRepository;

    public SemesterService(SemesterJpaRepository semesterRepository, AcademicYearJpaRepository academicYearRepository) {
        this.semesterRepository = semesterRepository;
        this.academicYearRepository = academicYearRepository;
    }

    public SemesterView createSemester(CreateSemesterCommand command) {
        // Verify academic year belongs to this organization
        academicYearRepository.findByIdAndOrganizationId(command.academicYearId(), command.organizationId())
                .orElseThrow(() -> new ResourceNotFoundException("AcademicYear", command.academicYearId().toString()));

        if (!command.endDate().isAfter(command.startDate())) {
            throw new InvalidAcademicPeriodException("End date must be after start date");
        }

        SemesterEntity entity = new SemesterEntity(
                UUID.randomUUID(),
                command.academicYearId(),
                command.name().trim(),
                command.startDate(),
                command.endDate(),
                command.organizationId()
        );

        SemesterEntity saved = semesterRepository.save(entity);
        return toView(saved);
    }

    @Transactional(readOnly = true)
    public List<SemesterView> listByAcademicYear(UUID organizationId, UUID academicYearId) {
        return semesterRepository.findAllByAcademicYearIdAndOrganizationId(academicYearId, organizationId)
                .stream()
                .map(this::toView)
                .toList();
    }

    @Transactional(readOnly = true)
    public SemesterView getById(UUID organizationId, UUID semesterId) {
        return semesterRepository.findByIdAndOrganizationId(semesterId, organizationId)
                .map(this::toView)
                .orElseThrow(() -> new ResourceNotFoundException("Semester", semesterId.toString()));
    }

    private SemesterView toView(SemesterEntity entity) {
        return new SemesterView(
                entity.getId(),
                entity.getAcademicYearId(),
                entity.getName(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getOrganizationId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}

