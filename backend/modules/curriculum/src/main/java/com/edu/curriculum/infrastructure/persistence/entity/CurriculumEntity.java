package com.edu.curriculum.infrastructure.persistence.entity;

import com.edu.shared.kernel.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.UUID;

@Entity
@Table(
        name = "curricula",
        schema = "curriculum",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_curriculum_code_per_org", columnNames = {"code", "organization_id"})
        }
)
public class CurriculumEntity extends AuditableEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "total_credits", nullable = false)
    private Integer totalCredits;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    protected CurriculumEntity() {
    }

    public CurriculumEntity(UUID id, String code, String name, String description,
                            Integer totalCredits, UUID organizationId) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.totalCredits = totalCredits;
        this.organizationId = organizationId;
    }

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getTotalCredits() {
        return totalCredits;
    }

    public void setTotalCredits(Integer totalCredits) {
        this.totalCredits = totalCredits;
    }

    public UUID getOrganizationId() {
        return organizationId;
    }
}

