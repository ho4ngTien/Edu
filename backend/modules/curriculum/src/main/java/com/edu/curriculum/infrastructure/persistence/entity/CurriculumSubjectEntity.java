package com.edu.curriculum.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "curriculum_subjects", schema = "curriculum")
@IdClass(CurriculumSubjectId.class)
public class CurriculumSubjectEntity {

    @Id
    @Column(name = "curriculum_id")
    private UUID curriculumId;

    @Id
    @Column(name = "subject_id")
    private UUID subjectId;

    @Column(name = "is_required", nullable = false)
    private Boolean isRequired;

    @Column(name = "sequence_order")
    private Integer sequenceOrder;

    @Column(name = "assigned_at", nullable = false, updatable = false)
    private Instant assignedAt;

    protected CurriculumSubjectEntity() {
    }

    public CurriculumSubjectEntity(UUID curriculumId, UUID subjectId,
                                   Boolean isRequired, Integer sequenceOrder) {
        this.curriculumId = curriculumId;
        this.subjectId = subjectId;
        this.isRequired = isRequired;
        this.sequenceOrder = sequenceOrder;
        this.assignedAt = Instant.now();
    }

    public UUID getCurriculumId() {
        return curriculumId;
    }

    public UUID getSubjectId() {
        return subjectId;
    }

    public Boolean getIsRequired() {
        return isRequired;
    }

    public void setIsRequired(Boolean isRequired) {
        this.isRequired = isRequired;
    }

    public Integer getSequenceOrder() {
        return sequenceOrder;
    }

    public void setSequenceOrder(Integer sequenceOrder) {
        this.sequenceOrder = sequenceOrder;
    }

    public Instant getAssignedAt() {
        return assignedAt;
    }
}

