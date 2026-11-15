package org.delcom.ifs25028springbootcashflow.adapter.repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.delcom.ifs25028springbootcashflow.domain.repository.IRepository;

/** Basis generik repository in-memory (khusus unit test). */
public abstract class InMemoryRepository<T> implements IRepository<T, Integer> {

    protected final Map<Integer, T> store = new LinkedHashMap<>();
    protected int nextId = 1;

    protected abstract int idOf(T entity);

    @Override
    public List<T> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public Optional<T> findById(Integer id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public boolean update(T entity) {
        int id = idOf(entity);
        if (!store.containsKey(id)) {
            return false;
        }
        store.put(id, entity);
        return true;
    }

    @Override
    public boolean deleteById(Integer id) {
        return store.remove(id) != null;
    }
}