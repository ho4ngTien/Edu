package com.edu.people.infrastructure.persistence.repository;

import com.edu.people.infrastructure.persistence.entity.TeacherEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TeacherJpaRepository extends JpaRepository<TeacherEntity, UUID> {
    boolean existsByTeacherCode(String teacherCode);
    List<TeacherEntity> findAllByOrganizationId(UUID organizationId);
    Optional<TeacherEntity> findByIdAndOrganizationId(UUID id, UUID organizationId);
}

