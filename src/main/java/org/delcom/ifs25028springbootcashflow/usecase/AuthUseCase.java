package org.delcom.ifs25028springbootcashflow.usecase;

import java.util.Optional;
import java.util.regex.Pattern;
import org.delcom.ifs25028springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25028springbootcashflow.domain.entity.Role;
import org.delcom.ifs25028springbootcashflow.domain.entity.User;
import org.delcom.ifs25028springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25028springbootcashflow.domain.repository.IUserRepository;

public class AuthUseCase {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("[A-Za-z0-9_.-]{3,30}");

    private final IUserRepository userRepository;

    public AuthUseCase(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User register(String username, String password, String fullName) throws ValidationException {
        String uname = username == null ? "" : username.trim();
        String name = fullName == null ? "" : fullName.trim();

        if (name.isEmpty()) {
            throw new ValidationException("Nama lengkap tidak boleh kosong");
        }
        if (!USERNAME_PATTERN.matcher(uname).matches()) {
            throw new ValidationException(
                    "Username 3-30 karakter, hanya huruf, angka, titik, garis bawah, dan strip");
        }
        if (password == null || password.length() < 6) {
            throw new ValidationException("Password minimal 6 karakter");
        }
        if (userRepository.findByUsername(uname).isPresent()) {
            throw new ValidationException("Username sudah digunakan");
        }
        return userRepository.save(uname, PasswordHasher.hash(password), name, Role.USER);
    }

    public User login(String username, String password) throws ValidationException {
        if (username == null || username.isBlank() || password == null || password.isEmpty()) {
            throw new ValidationException("Username dan password wajib diisi");
        }
        Optional<User> found = userRepository.findByUsername(username.trim());
        if (found.isEmpty() || !PasswordHasher.matches(password, found.get().getPasswordHash())) {
            throw new ValidationException("Username atau password salah");
        }
        return found.get();
    }

    /** Membuat akun admin dari ROOT_USERNAME / ROOT_PASSWORD bila belum ada. */
    public void ensureRootAdmin(String username, String password) {
        if (userRepository.findByUsername(username).isEmpty()) {
            userRepository.save(username, PasswordHasher.hash(password), "Administrator", Role.ADMIN);
        }
    }
}