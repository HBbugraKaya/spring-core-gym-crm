package com.example.gymcrm.generator;

public interface UsernameGenerator {
    String generate(String firstName, String lastName);

    default void confirm(String username) {
    }

    default void release(String username) {
    }
}
