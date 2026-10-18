package com.edu.shared.kernel.domain.event;

import java.time.Instant;
import java.util.UUID;

public interface DomainEvent {

    UUID eventId();

    Instant occurredAt();
}
