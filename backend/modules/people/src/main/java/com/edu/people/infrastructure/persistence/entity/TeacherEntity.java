package com.edu.people.infrastructure.persistence.entity;

import com.edu.people.domain.model.TeacherStatus;
import com.edu.shared.kernel.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.UUID;

@Entity
@Table(
        name = "teachers",
        schema = "people",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_teachers_code", columnNames = "teacher_code")
        }
)
public class TeacherEntity extends AuditableEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "teacher_code", nullable = false, length = 20)
    private String teacherCode;

    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @Column(length = 255)
    private String specialization;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TeacherStatus status;

    protected TeacherEntity() {
    }

    public TeacherEntity(UUID id, UUID userId, String teacherCode, String fullName,
                         String specialization, UUID organizationId, TeacherStatus status) {
        this.id = id;
        this.userId = userId;
        this.teacherCode = teacherCode;
        this.fullName = fullName;
        this.specialization = specialization;
        this.organizationId = organizationId;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getTeacherCode() {
        return teacherCode;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public UUID getOrganizationId() {
        return organizationId;
    }

    public TeacherStatus getStatus() {
        return status;
    }

    public void setStatus(TeacherStatus status) {
        this.status = status;
    }
}

