package org.delcom.ifs25028springbootcashflow.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public interface IRepository<T, ID> {

    List<T> findAll();

    Optional<T> findById(ID id);

    boolean update(T entity);

    boolean deleteById(ID id);

    default List<T> findBy(Predicate<T> filter) {
        return findAll().stream().filter(filter).toList();
    }
}