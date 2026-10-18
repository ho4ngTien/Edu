package com.edu.shared.kernel.domain.identifier;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrganizationIdTest {

    @Test
    @DisplayName("Nen tao hai OrganizationId ngau nhien khac nhau")
    void shouldGenerateUniqueIds() {
        OrganizationId id1 = OrganizationId.generate();
        OrganizationId id2 = OrganizationId.generate();

        assertNotNull(id1);
        assertNotNull(id2);
        assertNotEquals(id1, id2);
    }

    @Test
    @DisplayName("Nen tu choi gia tri UUID null")
    void shouldRejectNullValue() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new OrganizationId(null)
        );
        assertEquals("OrganizationId value must not be null", exception.getMessage());
    }

    @Test
    @DisplayName("Nen khoi tao thanh cong tu UUID hop le")
    void shouldCreateIdFromExistingUuid() {
        UUID rawUuid = UUID.randomUUID();
        OrganizationId id = OrganizationId.of(rawUuid);

        assertEquals(rawUuid, id.value());
        assertEquals(rawUuid.toString(), id.toString());
    }

    @Test
    @DisplayName("Nen khoi tao thanh cong tu chuoi String UUID")
    void shouldCreateIdFromString() {
        UUID rawUuid = UUID.randomUUID();
        OrganizationId id = OrganizationId.fromString(rawUuid.toString());

        assertEquals(rawUuid, id.value());
    }
}
