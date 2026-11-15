package org.delcom.ifs25028springbootcashflow.adapter.repository;

import java.util.Optional;
import org.delcom.ifs25028springbootcashflow.domain.entity.Role;
import org.delcom.ifs25028springbootcashflow.domain.entity.User;
import org.delcom.ifs25028springbootcashflow.domain.repository.IUserRepository;

/** Implementasi in-memory untuk unit test. */
public class UserRepository extends InMemoryRepository<User> implements IUserRepository {

    @Override
    protected int idOf(User entity) {
        return entity.getId();
    }

    @Override
    public User save(String username, String passwordHash, String fullName, Role role) {
        User user = new User(nextId++, username, passwordHash, fullName, role, null);
        store.put(user.getId(), user);
        return user;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return store.values().stream().filter(u -> u.getUsername().equals(username)).findFirst();
    }
}