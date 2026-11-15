package org.delcom.ifs25028springbootcashflow.adapter.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import javax.sql.DataSource;
import org.delcom.ifs25028springbootcashflow.domain.entity.Role;
import org.delcom.ifs25028springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25028springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25028springbootcashflow.support.TestDb;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcTransactionRepositoryTest {

    private JdbcTransactionRepository repo;
    private int userId;

    @BeforeEach
    void setUp() {
        DataSource ds = TestDb.newDataSource();
        userId = new JdbcUserRepository(ds).save("budi", "hash", "Budi", Role.USER).getId();
        repo = new JdbcTransactionRepository(ds);
    }

    @Test
    void simpanDanBaca() {
        Transaction masuk = repo.save(userId, "Gaji", 1_000_000, TransactionType.INCOME, null);
        Transaction keluar = repo.save(userId, "Kopi", 25_000, TransactionType.EXPENSE, "bukti.png");

        assertTrue(masuk.getId() > 0);

        List<Transaction> list = repo.findByUserId(userId);
        assertEquals(2, list.size());
        assertEquals("Kopi", list.get(0).getDescription()); // terbaru dulu
        assertEquals(0, repo.findByUserId(999).size());
        assertEquals(2, repo.findAll().size());

        Transaction found = repo.findById(keluar.getId()).orElseThrow();
        assertEquals(userId, found.getUserId());
        assertEquals(25_000, found.getAmount());
        assertEquals(TransactionType.EXPENSE, found.getType());
        assertEquals("bukti.png", found.getProofPath());
        assertNotNull(found.getCreatedAt());
        assertTrue(repo.findById(999).isEmpty());
    }

    @Test
    void perbarui() {
        Transaction t = repo.save(userId, "Gaji", 100, TransactionType.INCOME, null);
        t.setDescription("Bonus");
        t.setAmount(250);
        t.setType(TransactionType.EXPENSE);
        t.setProofPath("baru.png");

        assertTrue(repo.update(t));

        Transaction reload = repo.findById(t.getId()).orElseThrow();
        assertEquals("Bonus", reload.getDescription());
        assertEquals(250, reload.getAmount());
        assertEquals(TransactionType.EXPENSE, reload.getType());
        assertEquals("baru.png", reload.getProofPath());

        Transaction hantu = new Transaction(999, userId, "x", 1, TransactionType.INCOME, null, LocalDateTime.now());
        assertFalse(repo.update(hantu));
    }

    @Test
    void hapus() {
        Transaction t = repo.save(userId, "Gaji", 100, TransactionType.INCOME, null);
        assertTrue(repo.deleteById(t.getId()));
        assertFalse(repo.deleteById(t.getId()));
    }

    @Test
    void semuaOperasiGagalBilaKoneksiRusak() {
        JdbcTransactionRepository rusak = new JdbcTransactionRepository(TestDb.brokenDataSource());
        Transaction t = new Transaction(1, 1, "x", 1, TransactionType.INCOME, null, LocalDateTime.now());

        assertThrows(IllegalStateException.class,
                () -> rusak.save(1, "x", 1, TransactionType.INCOME, null));
        assertThrows(IllegalStateException.class, () -> rusak.findByUserId(1));
        assertThrows(IllegalStateException.class, rusak::findAll);
        assertThrows(IllegalStateException.class, () -> rusak.findById(1));
        assertThrows(IllegalStateException.class, () -> rusak.update(t));
        assertThrows(IllegalStateException.class, () -> rusak.deleteById(1));
    }
}