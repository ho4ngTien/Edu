package com.edu.people.infrastructure.persistence.repository;

import com.edu.people.infrastructure.persistence.entity.StudentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StudentJpaRepository extends JpaRepository<StudentEntity, UUID> {
    boolean existsByStudentCode(String studentCode);
    List<StudentEntity> findAllByOrganizationId(UUID organizationId);
    Optional<StudentEntity> findByIdAndOrganizationId(UUID id, UUID organizationId);
}

