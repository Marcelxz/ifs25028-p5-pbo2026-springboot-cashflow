package org.delcom.ifs25028springbootcashflow.domain.repository;

import java.util.Optional;
import org.delcom.ifs25028springbootcashflow.domain.entity.Role;
import org.delcom.ifs25028springbootcashflow.domain.entity.User;

public interface IUserRepository extends IRepository<User, Integer> {

    User save(String username, String passwordHash, String fullName, Role role);

    Optional<User> findByUsername(String username);
}