package com.edu.organization.infrastructure.persistence.repository;

import com.edu.organization.infrastructure.persistence.entity.OrganizationMembershipEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizationMembershipJpaRepository extends JpaRepository<OrganizationMembershipEntity, UUID> {

    boolean existsByOrganizationIdAndUserId(UUID organizationId, UUID userId);

    Optional<OrganizationMembershipEntity> findByOrganizationIdAndUserId(UUID organizationId, UUID userId);

    List<OrganizationMembershipEntity> findByOrganizationId(UUID organizationId);

    List<OrganizationMembershipEntity> findByUserId(UUID userId);
}
