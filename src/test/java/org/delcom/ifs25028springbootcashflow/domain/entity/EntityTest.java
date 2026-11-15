package org.delcom.ifs25028springbootcashflow.domain.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class EntityTest {

    @Test
    void userGetterSetter() {
        User user = new User(1, "budi", "hash", "Budi", Role.USER, null);
        assertEquals(1, user.getId());
        assertEquals("budi", user.getUsername());
        assertEquals("hash", user.getPasswordHash());
        assertEquals("Budi", user.getFullName());
        assertEquals(Role.USER, user.getRole());
        assertNull(user.getPhotoPath());
        assertFalse(user.isAdmin());

        user.setPasswordHash("hash2");
        user.setFullName("Budi Santoso");
        user.setPhotoPath("foto.png");
        assertEquals("hash2", user.getPasswordHash());
        assertEquals("Budi Santoso", user.getFullName());
        assertEquals("foto.png", user.getPhotoPath());

        assertTrue(new User(2, "admin", "h", "Admin", Role.ADMIN, null).isAdmin());
    }

    @Test
    void transactionGetterSetter() {
        LocalDateTime waktu = LocalDateTime.of(2026, 11, 15, 10, 0);
        Transaction t = new Transaction(1, 2, "Gaji", 1000, TransactionType.INCOME, null, waktu);

        assertEquals(1, t.getId());
        assertEquals(2, t.getUserId());
        assertEquals("Gaji", t.getDescription());
        assertEquals(1000, t.getAmount());
        assertEquals(TransactionType.INCOME, t.getType());
        assertNull(t.getProofPath());
        assertEquals(waktu, t.getCreatedAt());
        assertTrue(t.isIncome());
        assertEquals(1000, t.signedAmount());

        t.setDescription("Belanja");
        t.setAmount(500);
        t.setType(TransactionType.EXPENSE);
        t.setProofPath("bukti.png");

        assertEquals("Belanja", t.getDescription());
        assertEquals(500, t.getAmount());
        assertEquals("bukti.png", t.getProofPath());
        assertFalse(t.isIncome());
        assertEquals(-500, t.signedAmount());
        assertNotNull(t.getType());
    }
}