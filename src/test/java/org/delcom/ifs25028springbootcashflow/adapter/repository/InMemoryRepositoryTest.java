package org.delcom.ifs25028springbootcashflow.adapter.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import org.delcom.ifs25028springbootcashflow.domain.entity.Role;
import org.delcom.ifs25028springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25028springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25028springbootcashflow.domain.entity.User;
import org.junit.jupiter.api.Test;

class InMemoryRepositoryTest {

    @Test
    void transactionRepository() {
        TransactionRepository repo = new TransactionRepository();
        Transaction a = repo.save(1, "A", 100, TransactionType.INCOME, null);
        Transaction b = repo.save(2, "B", 200, TransactionType.EXPENSE, null);

        assertEquals(1, a.getId());
        assertEquals(2, b.getId());
        assertEquals(2, repo.findAll().size());
        assertTrue(repo.findById(1).isPresent());
        assertTrue(repo.findById(99).isEmpty());
        assertEquals(1, repo.findByUserId(1).size());
        assertEquals(1, repo.findBy(t -> t.getAmount() > 150).size());

        a.setDescription("A2");
        assertTrue(repo.update(a));
        assertEquals("A2", repo.findById(1).orElseThrow().getDescription());

        Transaction hantu = new Transaction(99, 1, "x", 1, TransactionType.INCOME, null, LocalDateTime.now());
        assertFalse(repo.update(hantu));

        assertTrue(repo.deleteById(1));
        assertFalse(repo.deleteById(1));
    }

    @Test
    void userRepository() {
        UserRepository repo = new UserRepository();
        User budi = repo.save("budi", "hash", "Budi", Role.USER);
        repo.save("ani", "hash", "Ani", Role.USER);

        assertEquals(1, budi.getId());
        assertEquals(2, repo.findAll().size());
        assertTrue(repo.findByUsername("budi").isPresent());
        assertTrue(repo.findByUsername("tidakada").isEmpty());
        assertTrue(repo.findById(1).isPresent());
        assertTrue(repo.findById(99).isEmpty());

        budi.setFullName("Budi Baru");
        assertTrue(repo.update(budi));
        assertEquals("Budi Baru", repo.findById(1).orElseThrow().getFullName());

        assertFalse(repo.update(new User(99, "x", "h", "X", Role.USER, null)));

        assertTrue(repo.deleteById(1));
        assertFalse(repo.deleteById(1));
    }
}