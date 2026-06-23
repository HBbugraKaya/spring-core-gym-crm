package com.example.gymcrm.exception;

public final class EntityNotFoundException extends GymCrmException {
    public EntityNotFoundException(String entityType, Long id) {
        super(entityType + " was not found for id=" + id);
    }
}
