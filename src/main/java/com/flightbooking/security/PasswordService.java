package com.flightbooking.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordService {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public String hashPassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException(
                    "Password cannot be empty");
        }
        return encoder.encode(password);
    }

    public boolean verifyPassword(String rawPassword, String storedHash) {
        if (rawPassword == null || storedHash == null) {
            return false;
        }
        return encoder.matches(rawPassword, storedHash);
    }
}
