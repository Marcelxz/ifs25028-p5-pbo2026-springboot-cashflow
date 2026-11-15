package org.delcom.ifs25028springbootcashflow.adapter.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.delcom.ifs25028springbootcashflow.domain.entity.Role;
import org.delcom.ifs25028springbootcashflow.domain.entity.User;
import org.delcom.ifs25028springbootcashflow.support.TestDb;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcUserRepositoryTest {

    private JdbcUserRepository repo;

    @BeforeEach
    void setUp() {
        repo = new JdbcUserRepository(TestDb.newDataSource());
    }

    @Test
    void simpanDanCari() {
        User budi = repo.save("budi", "hash", "Budi", Role.USER);
        repo.save("admin", "hash", "Admin", Role.ADMIN);

        assertTrue(budi.getId() > 0);
        assertNull(budi.getPhotoPath());

        User found = repo.findByUsername("budi").orElseThrow();
        assertEquals(budi.getId(), found.getId());
        assertEquals("hash", found.getPasswordHash());
        assertEquals("Budi", found.getFullName());
        assertEquals(Role.USER, found.getRole());
        assertTrue(repo.findByUsername("tidakada").isEmpty());

        assertEquals("budi", repo.findById(budi.getId()).orElseThrow().getUsername());
        assertTrue(repo.findById(999).isEmpty());

        assertEquals(2, repo.findAll().size());
    }

    @Test
    void perbarui() {
        User budi = repo.save("budi", "hash", "Budi", Role.USER);
        budi.setFullName("Budi Baru");
        budi.setPasswordHash("hash2");
        budi.setPhotoPath("foto.png");

        assertTrue(repo.update(budi));

        User reload = repo.findById(budi.getId()).orElseThrow();
        assertEquals("Budi Baru", reload.getFullName());
        assertEquals("hash2", reload.getPasswordHash());
        assertEquals("foto.png", reload.getPhotoPath());

        assertFalse(repo.update(new User(999, "x", "h", "X", Role.USER, null)));
    }

    @Test
    void hapus() {
        User budi = repo.save("budi", "hash", "Budi", Role.USER);
        assertTrue(repo.deleteById(budi.getId()));
        assertFalse(repo.deleteById(budi.getId()));
    }

    @Test
    void semuaOperasiGagalBilaKoneksiRusak() {
        JdbcUserRepository rusak = new JdbcUserRepository(TestDb.brokenDataSource());
        User user = new User(1, "a", "h", "A", Role.USER, null);

        assertThrows(IllegalStateException.class, () -> rusak.save("a", "h", "A", Role.USER));
        assertThrows(IllegalStateException.class, () -> rusak.findByUsername("a"));
        assertThrows(IllegalStateException.class, rusak::findAll);
        assertThrows(IllegalStateException.class, () -> rusak.findById(1));
        assertThrows(IllegalStateException.class, () -> rusak.update(user));
        assertThrows(IllegalStateException.class, () -> rusak.deleteById(1));
    }
}