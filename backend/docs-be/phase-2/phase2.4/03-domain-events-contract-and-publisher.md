# Phase 2.4 — Thành phần 03: Domain Events Contract & Publisher

Tài liệu này đặc tả hợp đồng sự kiện nghiệp vụ `DomainEvent` và cơ chế phát tán sự kiện trong E-Edu.

---

## 1. Interface `DomainEvent`

Tệp đặt tại: `shared/shared-kernel/src/main/java/com/edu/shared/kernel/domain/event/DomainEvent.java`

```java
package com.edu.shared.kernel.domain.event;

import java.time.Instant;
import java.util.UUID;

public interface DomainEvent {
    UUID eventId();
    Instant occurredAt();
    String eventType();
}
```

---

## 2. Hướng Dẫn Định Nghĩa Domain Event Cụ Thể

Khi một module phát sinh sự kiện, tạo Java Record thực thi `DomainEvent`:

```java
package com.edu.organization.domain.event;

import com.edu.shared.kernel.domain.event.DomainEvent;
import com.edu.shared.kernel.domain.identifier.OrganizationId;
import java.time.Instant;
import java.util.UUID;

public record OrganizationCreatedEvent(
    UUID eventId,
    Instant occurredAt,
    OrganizationId organizationId,
    String name,
    String code
) implements DomainEvent {

    public OrganizationCreatedEvent(OrganizationId organizationId, String name, String code) {
        this(UUID.randomUUID(), Instant.now(), organizationId, name, code);
    }

    @Override
    public String eventType() {
        return "OrganizationCreated";
    }
}
```

---

## 3. Phát Tán và Lắng Nghe Sự Kiện

### 3.1. Phát tán sự kiện tại Application Service:
```java
@Service
@Transactional
public class OrganizationService {
    private final ApplicationEventPublisher eventPublisher;

    public OrganizationResponse createOrganization(CreateOrganizationCommand command) {
        // ... lưu database
        eventPublisher.publishEvent(new OrganizationCreatedEvent(orgId, name, code));
        return response;
    }
}
```

### 3.2. Lắng nghe sự kiện tại module khác sau khi commit:
```java
@Component
public class OrganizationEventListener {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrganizationCreated(OrganizationCreatedEvent event) {
        // Chỉ chạy khi transaction của organization đã commit thành công vào CSDL!
    }
}
```
