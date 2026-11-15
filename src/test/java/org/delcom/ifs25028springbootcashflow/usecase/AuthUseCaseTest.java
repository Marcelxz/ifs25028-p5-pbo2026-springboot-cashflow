package org.delcom.ifs25028springbootcashflow.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.delcom.ifs25028springbootcashflow.adapter.repository.UserRepository;
import org.delcom.ifs25028springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25028springbootcashflow.domain.entity.Role;
import org.delcom.ifs25028springbootcashflow.domain.entity.User;
import org.delcom.ifs25028springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AuthUseCaseTest {

    private UserRepository users;
    private AuthUseCase useCase;

    @BeforeEach
    void setUp() {
        users = new UserRepository();
        useCase = new AuthUseCase(users);
    }

    @Test
    void registerBerhasil() throws Exception {
        User user = useCase.register(" budi ", "rahasia1", " Budi Santoso ");
        assertEquals("budi", user.getUsername());
        assertEquals("Budi Santoso", user.getFullName());
        assertEquals(Role.USER, user.getRole());
        assertEquals(PasswordHasher.hash("rahasia1"), user.getPasswordHash());
    }

    @Test
    void registerNamaKosong() {
        assertThrows(ValidationException.class, () -> useCase.register("budi", "rahasia1", null));
        assertThrows(ValidationException.class, () -> useCase.register("budi", "rahasia1", "  "));
    }

    @Test
    void registerUsernameTidakValid() {
        assertThrows(ValidationException.class, () -> useCase.register(null, "rahasia1", "Budi"));
        assertThrows(ValidationException.class, () -> useCase.register("ab", "rahasia1", "Budi"));
        assertThrows(ValidationException.class, () -> useCase.register("bu di", "rahasia1", "Budi"));
    }

    @Test
    void registerPasswordTidakValid() {
        assertThrows(ValidationException.class, () -> useCase.register("budi", null, "Budi"));
        assertThrows(ValidationException.class, () -> useCase.register("budi", "123", "Budi"));
    }

    @Test
    void registerUsernameSudahDipakai() throws Exception {
        useCase.register("budi", "rahasia1", "Budi");
        assertThrows(ValidationException.class, () -> useCase.register("budi", "rahasia2", "Budi Lain"));
    }

    @Test
    void loginBerhasil() throws Exception {
        useCase.register("budi", "rahasia1", "Budi");
        User user = useCase.login(" budi ", "rahasia1");
        assertEquals("budi", user.getUsername());
    }

    @Test
    void loginInputKosong() {
        assertThrows(ValidationException.class, () -> useCase.login(null, "x"));
        assertThrows(ValidationException.class, () -> useCase.login("  ", "x"));
        assertThrows(ValidationException.class, () -> useCase.login("budi", null));
        assertThrows(ValidationException.class, () -> useCase.login("budi", ""));
    }

    @Test
    void loginSalah() throws Exception {
        useCase.register("budi", "rahasia1", "Budi");
        assertThrows(ValidationException.class, () -> useCase.login("tidakada", "rahasia1"));
        assertThrows(ValidationException.class, () -> useCase.login("budi", "salah"));
    }

    @Test
    void ensureRootAdminHanyaMembuatSekali() {
        useCase.ensureRootAdmin("admin", "admin");
        useCase.ensureRootAdmin("admin", "admin");
        assertEquals(1, users.findAll().size());
        assertEquals(Role.ADMIN, users.findByUsername("admin").orElseThrow().getRole());
    }
}