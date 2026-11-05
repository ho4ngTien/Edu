package com.edu.curriculum.infrastructure.persistence.entity;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class CurriculumSubjectId implements Serializable {

    private UUID curriculumId;
    private UUID subjectId;

    public CurriculumSubjectId() {
    }

    public CurriculumSubjectId(UUID curriculumId, UUID subjectId) {
        this.curriculumId = curriculumId;
        this.subjectId = subjectId;
    }

    public UUID getCurriculumId() {
        return curriculumId;
    }

    public void setCurriculumId(UUID curriculumId) {
        this.curriculumId = curriculumId;
    }

    public UUID getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(UUID subjectId) {
        this.subjectId = subjectId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CurriculumSubjectId that)) return false;
        return Objects.equals(curriculumId, that.curriculumId) && Objects.equals(subjectId, that.subjectId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(curriculumId, subjectId);
    }
}

