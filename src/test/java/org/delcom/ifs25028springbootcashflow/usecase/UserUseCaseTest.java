package org.delcom.ifs25028springbootcashflow.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.delcom.ifs25028springbootcashflow.adapter.repository.UserRepository;
import org.delcom.ifs25028springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25028springbootcashflow.domain.entity.Role;
import org.delcom.ifs25028springbootcashflow.domain.entity.User;
import org.delcom.ifs25028springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25028springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserUseCaseTest {

    private UserUseCase useCase;
    private int userId;
    private String hashLama;

    @BeforeEach
    void setUp() {
        UserRepository users = new UserRepository();
        hashLama = PasswordHasher.hash("lama123");
        userId = users.save("budi", hashLama, "Budi", Role.USER).getId();
        useCase = new UserUseCase(users);
    }

    @Test
    void daftarDanAmbilUser() throws Exception {
        assertEquals(1, useCase.listUsers().size());
        assertEquals("budi", useCase.getUser(userId).getUsername());
        assertThrows(EntityNotFoundException.class, () -> useCase.getUser(99));
    }

    @Test
    void updateProfilTanpaGantiPassword() throws Exception {
        User user = useCase.updateProfile(userId, " Budi Baru ", null);
        assertEquals("Budi Baru", user.getFullName());
        assertEquals(hashLama, user.getPasswordHash());

        user = useCase.updateProfile(userId, "Budi Lagi", "");
        assertEquals("Budi Lagi", user.getFullName());
        assertEquals(hashLama, user.getPasswordHash());
    }

    @Test
    void updateProfilDenganPasswordBaru() throws Exception {
        User user = useCase.updateProfile(userId, "Budi", "baru1234");
        assertNotEquals(hashLama, user.getPasswordHash());
        assertEquals(PasswordHasher.hash("baru1234"), user.getPasswordHash());
    }

    @Test
    void updateProfilTidakValid() {
        assertThrows(ValidationException.class, () -> useCase.updateProfile(userId, null, null));
        assertThrows(ValidationException.class, () -> useCase.updateProfile(userId, "  ", null));
        assertThrows(ValidationException.class, () -> useCase.updateProfile(userId, "Budi", "123"));
        assertThrows(EntityNotFoundException.class, () -> useCase.updateProfile(99, "Budi", null));
    }

    @Test
    void updateFoto() throws Exception {
        User user = useCase.updatePhoto(userId, "foto.png");
        assertEquals("foto.png", user.getPhotoPath());
        assertThrows(EntityNotFoundException.class, () -> useCase.updatePhoto(99, "foto.png"));
    }
}