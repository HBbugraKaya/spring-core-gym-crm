package com.example.gymcrm.exception;

public class GymCrmException extends RuntimeException {
    public GymCrmException(String message) {
        super(message);
    }

    public GymCrmException(String message, Throwable cause) {
        super(message, cause);
    }
}
