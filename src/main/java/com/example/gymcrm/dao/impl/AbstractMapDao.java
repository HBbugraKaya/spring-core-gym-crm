package com.example.gymcrm.dao.impl;

import com.example.gymcrm.exception.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

abstract class AbstractMapDao<T> {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final AtomicLong sequence = new AtomicLong();
    private Map<Long, T> storage;

    protected final void initializeStorage(Map<Long, T> storage) {
        this.storage = storage;
        long highestId = storage.keySet().stream().mapToLong(Long::longValue).max().orElse(0L);
        sequence.set(highestId);
    }

    protected final T createEntity(T entity) {
        requireStorage();
        long id = sequence.incrementAndGet();
        setId(entity, id);
        storage.put(id, entity);
        logger.info("Created {} id={}", entityType(), id);
        return entity;
    }

    protected final T updateEntity(T entity) {
        requireStorage();
        Long id = getId(entity);
        if (id == null || storage.replace(id, entity) == null) {
            logger.warn("Cannot update missing {} id={}", entityType(), id);
            throw new EntityNotFoundException(entityType(), id);
        }
        logger.info("Updated {} id={}", entityType(), id);
        return entity;
    }

    protected final void deleteEntity(Long id) {
        requireStorage();
        if (id == null || storage.remove(id) == null) {
            logger.warn("Cannot delete missing {} id={}", entityType(), id);
            throw new EntityNotFoundException(entityType(), id);
        }
        logger.info("Deleted {} id={}", entityType(), id);
    }

    protected final Optional<T> findEntity(Long id) {
        requireStorage();
        logger.debug("Finding {} id={}", entityType(), id);
        return Optional.ofNullable(storage.get(id));
    }

    protected final List<T> findAllEntities() {
        requireStorage();
        List<T> entities = storage.values().stream()
                .sorted(Comparator.comparing(this::getId))
                .toList();
        logger.debug("Listed {} count={}", entityType(), entities.size());
        return entities;
    }

    protected abstract String entityType();

    protected abstract Long getId(T entity);

    protected abstract void setId(T entity, Long id);

    private void requireStorage() {
        if (storage == null) {
            throw new IllegalStateException(entityType() + " storage has not been injected");
        }
    }
}
