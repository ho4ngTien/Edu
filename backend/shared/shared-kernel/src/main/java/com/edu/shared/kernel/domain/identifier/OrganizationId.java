package com.edu.shared.kernel.domain.identifier;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public record OrganizationId(UUID value) implements Serializable {

    public OrganizationId {
        Objects.requireNonNull(value, "OrganizationId value must not be null");
    }

    public static OrganizationId generate() {
        return new OrganizationId(UUID.randomUUID());
    }

    public static OrganizationId of(UUID value) {
        return new OrganizationId(value);
    }

    public static OrganizationId fromString(String value) {
        Objects.requireNonNull(value, "OrganizationId string must not be null");
        return new OrganizationId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
