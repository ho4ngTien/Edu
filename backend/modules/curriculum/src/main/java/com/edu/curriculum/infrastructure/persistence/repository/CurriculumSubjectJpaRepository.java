package com.edu.curriculum.infrastructure.persistence.repository;

import com.edu.curriculum.infrastructure.persistence.entity.CurriculumSubjectEntity;
import com.edu.curriculum.infrastructure.persistence.entity.CurriculumSubjectId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CurriculumSubjectJpaRepository extends JpaRepository<CurriculumSubjectEntity, CurriculumSubjectId> {
    List<CurriculumSubjectEntity> findAllByCurriculumId(UUID curriculumId);
    boolean existsByCurriculumIdAndSubjectId(UUID curriculumId, UUID subjectId);
    void deleteByCurriculumIdAndSubjectId(UUID curriculumId, UUID subjectId);
}

