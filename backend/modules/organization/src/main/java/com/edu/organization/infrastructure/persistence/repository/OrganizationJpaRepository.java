package com.edu.organization.infrastructure.persistence.repository;

import com.edu.organization.infrastructure.persistence.entity.OrganizationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizationJpaRepository extends JpaRepository<OrganizationEntity, UUID> {

    boolean existsByCode(String code);

    Optional<OrganizationEntity> findByCode(String code);
}
