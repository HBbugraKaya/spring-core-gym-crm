package com.example.gymcrm.exception;

public final class StorageInitializationException extends GymCrmException {
    public StorageInitializationException(String message) {
        super(message);
    }

    public StorageInitializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
