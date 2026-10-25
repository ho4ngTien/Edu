# Phase 2.5: JPA Entities & Repositories

> Thuộc [Phase 2.5: Identity & Organization Core](README.md)

### 4.2. JPA Entities & Repositories

#### 1. Entity Người dùng: `UserEntity`

Trong `modules/identity/src/main/java/com/edu/identity/infrastructure/persistence/entity/UserEntity.java`:

```java
package com.edu.identity.infrastructure.persistence.entity;

import com.edu.identity.domain.model.UserStatus;
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
        name = "users",
        schema = "identity",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_username", columnNames = "username"),
                @UniqueConstraint(name = "uk_users_email", columnNames = "email")
        }
)
public class UserEntity extends AuditableEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 100)
    private String username;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserStatus status;

    protected UserEntity() {
    }

    public UserEntity(UUID id, String username, String email, String passwordHash, UserStatus status) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}
```

#### 2. Entity Tổ chức: `OrganizationEntity`

Trong `modules/organization/src/main/java/com/edu/organization/infrastructure/persistence/entity/OrganizationEntity.java`:

```java
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
```

#### 3. Entity Tư cách thành viên: `OrganizationMembershipEntity`

Trong `modules/organization/src/main/java/com/edu/organization/infrastructure/persistence/entity/OrganizationMembershipEntity.java`:

```java
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
```

---
