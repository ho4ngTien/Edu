package com.edu.curriculum.infrastructure.persistence.repository;

import com.edu.curriculum.infrastructure.persistence.entity.CurriculumEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CurriculumJpaRepository extends JpaRepository<CurriculumEntity, UUID> {
    boolean existsByCodeAndOrganizationId(String code, UUID organizationId);
    List<CurriculumEntity> findAllByOrganizationId(UUID organizationId);
    Optional<CurriculumEntity> findByIdAndOrganizationId(UUID id, UUID organizationId);
}

