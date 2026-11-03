package com.edu.academic.infrastructure.persistence.repository;

import com.edu.academic.infrastructure.persistence.entity.AcademicYearEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AcademicYearJpaRepository extends JpaRepository<AcademicYearEntity, UUID> {
    List<AcademicYearEntity> findAllByOrganizationId(UUID organizationId);
    Optional<AcademicYearEntity> findByIdAndOrganizationId(UUID id, UUID organizationId);
}

