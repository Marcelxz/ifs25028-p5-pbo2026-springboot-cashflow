package org.delcom.ifs25028springbootcashflow.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PasswordHasherTest {

    @Test
    void hashSha256Hex() {
        assertEquals("ef797c8118f02dfb649607dd5d3f8c7623048c9c063d532cc95c5ed7a898a64f",
                PasswordHasher.hash("12345678"));
    }

    @Test
    void matchesBenarDanSalah() {
        String hash = PasswordHasher.hash("rahasia");
        assertTrue(PasswordHasher.matches("rahasia", hash));
        assertFalse(PasswordHasher.matches("salah", hash));
    }

    @Test
    void algoritmaTidakAda() {
        assertThrows(IllegalStateException.class, () -> PasswordHasher.digest("x", "BUKAN-ALGORITMA"));
    }
}