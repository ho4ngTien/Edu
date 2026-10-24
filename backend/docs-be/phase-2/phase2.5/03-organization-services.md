# Phase 2.5: Organization Application Services

> Thuộc [Phase 2.5: Identity & Organization Core](README.md)

### 4.3. Organization Application Services

#### 1. Public Port: `OrganizationAccountQuery`

Module `organization` không phụ thuộc vào `identity` repository mà khai báo interface port:

```java
package com.edu.organization.application.port;

import java.util.UUID;

public interface OrganizationAccountQuery {

    boolean accountExists(UUID userId);

    boolean accountCanJoinOrganization(UUID userId);
}
```

#### 2. DTOs & Commands trong `organization`

```java
package com.edu.organization.application.command;

public record CreateOrganizationCommand(String code, String name) {}

public record UpdateOrganizationCommand(String name) {}

public record InviteMemberCommand(UUID organizationId, UUID userId, OrganizationRole role) {}

public record UpdateMemberStatusCommand(UUID organizationId, UUID userId, MembershipStatus newStatus) {}
```

#### 3. Triển khai `OrganizationService`

```java
package com.edu.organization.application.service;

import com.edu.organization.application.command.CreateOrganizationCommand;
import com.edu.organization.application.command.UpdateOrganizationCommand;
import com.edu.organization.api.dto.OrganizationView;
import com.edu.organization.domain.exception.DuplicateOrganizationCodeException;
import com.edu.organization.domain.exception.OrganizationNotFoundException;
import com.edu.organization.domain.model.OrganizationStatus;
import com.edu.organization.infrastructure.persistence.entity.OrganizationEntity;
import com.edu.organization.infrastructure.persistence.repository.OrganizationJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class OrganizationService {

    private final OrganizationJpaRepository organizationRepository;

    public OrganizationService(OrganizationJpaRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    public OrganizationView createOrganization(CreateOrganizationCommand command) {
        if (organizationRepository.existsByCode(command.code())) {
            throw new DuplicateOrganizationCodeException(command.code());
        }

        OrganizationEntity entity = new OrganizationEntity(
                UUID.randomUUID(),
                command.code().trim().toUpperCase(),
                command.name().trim(),
                OrganizationStatus.ACTIVE
        );

        OrganizationEntity saved = organizationRepository.save(entity);
        return toView(saved);
    }

    @Transactional(readOnly = true)
    public OrganizationView getOrganization(UUID organizationId) {
        return organizationRepository.findById(organizationId)
                .map(this::toView)
                .orElseThrow(() -> new OrganizationNotFoundException(organizationId));
    }

    public OrganizationView updateOrganization(UUID organizationId, UpdateOrganizationCommand command) {
        OrganizationEntity entity = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new OrganizationNotFoundException(organizationId));

        entity.setName(command.name().trim());
        return toView(entity);
    }

    private OrganizationView toView(OrganizationEntity entity) {
        return new OrganizationView(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                entity.getStatus(),
                entity.getCreatedAt()
        );
    }
}
```

#### 4. Triển khai `MembershipService`

```java
package com.edu.organization.application.service;

import com.edu.organization.application.command.InviteMemberCommand;
import com.edu.organization.application.command.UpdateMemberStatusCommand;
import com.edu.organization.api.dto.MembershipView;
import com.edu.organization.application.port.OrganizationAccountQuery;
import com.edu.organization.domain.exception.MembershipConflictException;
import com.edu.organization.domain.exception.OrganizationNotFoundException;
import com.edu.organization.domain.model.MembershipStatus;
import com.edu.organization.infrastructure.persistence.entity.OrganizationMembershipEntity;
import com.edu.organization.infrastructure.persistence.repository.OrganizationJpaRepository;
import com.edu.organization.infrastructure.persistence.repository.OrganizationMembershipJpaRepository;
import com.edu.shared.kernel.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class MembershipService {

    private final OrganizationJpaRepository organizationRepository;
    private final OrganizationMembershipJpaRepository membershipRepository;
    private final OrganizationAccountQuery accountQuery;

    public MembershipService(
            OrganizationJpaRepository organizationRepository,
            OrganizationMembershipJpaRepository membershipRepository,
            OrganizationAccountQuery accountQuery
    ) {
        this.organizationRepository = organizationRepository;
        this.membershipRepository = membershipRepository;
        this.accountQuery = accountQuery;
    }

    public MembershipView inviteMember(InviteMemberCommand command) {
        if (!organizationRepository.existsById(command.organizationId())) {
            throw new OrganizationNotFoundException(command.organizationId());
        }

        if (!accountQuery.accountExists(command.userId()) || !accountQuery.accountCanJoinOrganization(command.userId())) {
            throw new ResourceNotFoundException("User Account", command.userId().toString());
        }

        if (membershipRepository.existsByOrganizationIdAndUserId(command.organizationId(), command.userId())) {
            throw new MembershipConflictException("User is already a member of this organization");
        }

        OrganizationMembershipEntity entity = new OrganizationMembershipEntity(
                UUID.randomUUID(),
                command.organizationId(),
                command.userId(),
                MembershipStatus.INVITED,
                command.role(),
                null
        );

        OrganizationMembershipEntity saved = membershipRepository.save(entity);
        return toView(saved);
    }

    public MembershipView updateMemberStatus(UpdateMemberStatusCommand command) {
        OrganizationMembershipEntity entity = membershipRepository
                .findByOrganizationIdAndUserId(command.organizationId(), command.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Membership", command.userId().toString()));

        entity.setStatus(command.newStatus());
        if (command.newStatus() == MembershipStatus.ACTIVE && entity.getJoinedAt() == null) {
            entity.setJoinedAt(Instant.now());
        }

        return toView(entity);
    }

    @Transactional(readOnly = true)
    public List<MembershipView> listMembers(UUID organizationId) {
        return membershipRepository.findByOrganizationId(organizationId).stream()
                .map(this::toView)
                .toList();
    }

    private MembershipView toView(OrganizationMembershipEntity entity) {
        return new MembershipView(
                entity.getId(),
                entity.getOrganizationId(),
                entity.getUserId(),
                entity.getRole(),
                entity.getStatus(),
                entity.getJoinedAt()
        );
    }
}
```

---
