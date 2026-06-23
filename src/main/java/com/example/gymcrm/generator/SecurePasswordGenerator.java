package com.example.gymcrm.generator;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class SecurePasswordGenerator implements PasswordGenerator {
    static final int PASSWORD_LENGTH = 10;
    private static final char[] ALPHANUMERIC =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String generate() {
        char[] password = new char[PASSWORD_LENGTH];
        for (int i = 0; i < password.length; i++) {
            password[i] = ALPHANUMERIC[secureRandom.nextInt(ALPHANUMERIC.length)];
        }
        return new String(password);
    }
}
