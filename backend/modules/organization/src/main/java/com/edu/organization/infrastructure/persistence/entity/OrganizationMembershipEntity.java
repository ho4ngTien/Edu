package com.edu.organization.infrastructure.persistence.entity;

import com.edu.organization.domain.model.MembershipStatus;
import com.edu.organization.domain.model.OrganizationRole;
import com.edu.shared.kernel.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "organization_memberships",
        schema = "organization",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_memberships_organization_user", columnNames = {"organization_id", "user_id"})
        }
)
public class OrganizationMembershipEntity extends AuditableEntity {

    @Id
    private UUID id;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MembershipStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrganizationRole role;

    @Column(name = "joined_at")
    private Instant joinedAt;

    protected OrganizationMembershipEntity() {
    }

    public OrganizationMembershipEntity(
            UUID id,
            UUID organizationId,
            UUID userId,
            MembershipStatus status,
            OrganizationRole role,
            Instant joinedAt
    ) {
        this.id = id;
        this.organizationId = organizationId;
        this.userId = userId;
        this.status = status;
        this.role = role;
        this.joinedAt = joinedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrganizationId() {
        return organizationId;
    }

    public UUID getUserId() {
        return userId;
    }

    public MembershipStatus getStatus() {
        return status;
    }

    public void setStatus(MembershipStatus status) {
        this.status = status;
    }

    public OrganizationRole getRole() {
        return role;
    }

    public void setRole(OrganizationRole role) {
        this.role = role;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(Instant joinedAt) {
        this.joinedAt = joinedAt;
    }
}
