package com.edu.academic.infrastructure.persistence.entity;

import com.edu.shared.kernel.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "semesters", schema = "academic")
public class SemesterEntity extends AuditableEntity {

    @Id
    private UUID id;

    @Column(name = "academic_year_id", nullable = false)
    private UUID academicYearId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    protected SemesterEntity() {
    }

    public SemesterEntity(UUID id, UUID academicYearId, String name,
                          LocalDate startDate, LocalDate endDate, UUID organizationId) {
        this.id = id;
        this.academicYearId = academicYearId;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.organizationId = organizationId;
    }

    public UUID getId() {
        return id;
    }

    public UUID getAcademicYearId() {
        return academicYearId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public UUID getOrganizationId() {
        return organizationId;
    }
}

