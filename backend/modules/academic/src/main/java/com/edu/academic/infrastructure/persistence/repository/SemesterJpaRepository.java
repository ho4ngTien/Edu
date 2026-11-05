package com.edu.academic.infrastructure.persistence.repository;

import com.edu.academic.infrastructure.persistence.entity.SemesterEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SemesterJpaRepository extends JpaRepository<SemesterEntity, UUID> {
    List<SemesterEntity> findAllByAcademicYearIdAndOrganizationId(UUID academicYearId, UUID organizationId);
    Optional<SemesterEntity> findByIdAndOrganizationId(UUID id, UUID organizationId);
}

