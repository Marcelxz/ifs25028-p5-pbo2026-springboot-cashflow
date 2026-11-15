package org.delcom.ifs25028springbootcashflow.usecase;

import java.util.List;
import org.delcom.ifs25028springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25028springbootcashflow.domain.entity.User;
import org.delcom.ifs25028springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25028springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25028springbootcashflow.domain.repository.IUserRepository;

public class UserUseCase {

    private final IUserRepository userRepository;

    public UserUseCase(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> listUsers() {
        return userRepository.findAll();
    }

    public User getUser(int id) throws EntityNotFoundException {
        return userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User", id));
    }

    /** newPassword boleh kosong/null bila password tidak diubah. */
    public User updateProfile(int id, String fullName, String newPassword)
            throws EntityNotFoundException, ValidationException {
        User user = getUser(id);
        String name = fullName == null ? "" : fullName.trim();
        if (name.isEmpty()) {
            throw new ValidationException("Nama lengkap tidak boleh kosong");
        }
        user.setFullName(name);
        if (newPassword != null && !newPassword.isEmpty()) {
            if (newPassword.length() < 6) {
                throw new ValidationException("Password minimal 6 karakter");
            }
            user.setPasswordHash(PasswordHasher.hash(newPassword));
        }
        userRepository.update(user);
        return user;
    }

    public User updatePhoto(int id, String photoPath) throws EntityNotFoundException {
        User user = getUser(id);
        user.setPhotoPath(photoPath);
        userRepository.update(user);
        return user;
    }
}