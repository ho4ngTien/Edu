package com.edu.curriculum.infrastructure.persistence.repository;

import com.edu.curriculum.infrastructure.persistence.entity.SubjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubjectJpaRepository extends JpaRepository<SubjectEntity, UUID> {
    boolean existsByCodeAndOrganizationId(String code, UUID organizationId);
    List<SubjectEntity> findAllByOrganizationId(UUID organizationId);
    Optional<SubjectEntity> findByIdAndOrganizationId(UUID id, UUID organizationId);
}

