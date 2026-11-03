package com.edu.academic.infrastructure.persistence.entity;

import com.edu.shared.kernel.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.UUID;

@Entity
@Table(
        name = "class_rooms",
        schema = "academic",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_class_code_per_org", columnNames = {"code", "organization_id"})
        }
)
public class ClassRoomEntity extends AuditableEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "academic_year_id", nullable = false)
    private UUID academicYearId;

    @Column
    private Integer capacity;

    protected ClassRoomEntity() {
    }

    public ClassRoomEntity(UUID id, String code, String name, UUID organizationId,
                           UUID academicYearId, Integer capacity) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.organizationId = organizationId;
        this.academicYearId = academicYearId;
        this.capacity = capacity;
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

    public UUID getOrganizationId() {
        return organizationId;
    }

    public UUID getAcademicYearId() {
        return academicYearId;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }
}

