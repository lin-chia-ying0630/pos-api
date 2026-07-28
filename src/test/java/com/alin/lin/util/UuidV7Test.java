package com.alin.lin.util;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class UuidV7Test {
    @Test
    void generatesUniqueVersion7Variant2Identifiers() {
        UUID first = UUID.fromString(UuidV7.next());
        UUID second = UUID.fromString(UuidV7.next());

        assertEquals(7, first.version());
        assertEquals(2, first.variant());
        assertNotEquals(first, second);
    }
}
