package com.edu.organization.infrastructure.persistence.entity;

import com.edu.organization.domain.model.OrganizationStatus;
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
        name = "organizations",
        schema = "organization",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_organizations_code", columnNames = "code")
        }
)
public class OrganizationEntity extends AuditableEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrganizationStatus status;

    protected OrganizationEntity() {
    }

    public OrganizationEntity(UUID id, String code, String name, OrganizationStatus status) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public OrganizationStatus getStatus() {
        return status;
    }

    public void setStatus(OrganizationStatus status) {
        this.status = status;
    }
}
