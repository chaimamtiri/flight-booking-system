package com.flightbooking.service;

import com.flightbooking.Passenger;
import com.flightbooking.model.AppUser;
import com.flightbooking.repository.PassengerRepository;
import com.flightbooking.repository.UserRepository;
import com.flightbooking.security.PasswordService;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PassengerRepository passengerRepository;
    private final PasswordService passwordService;

    public UserService(UserRepository userRepository, PassengerRepository passengerRepository, PasswordService passwordService) {
        this.userRepository = userRepository;
        this.passengerRepository = passengerRepository;
        this.passwordService = passwordService;
    }

    // Only call this after passenger identity/email
    // ownership has been verified by a trusted process.
    public AppUser createAccountForVerifiedPassenger(String passengerId, String email, String password) throws SQLException {
        if (passengerId == null || passengerId.isBlank()) {
            throw new IllegalArgumentException("Passenger ID is required");
        }

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (password == null || password.length() < 12 || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("Password must contain at least 12 characters " + "and no more than 72 UTF-8 bytes");
        }

        Passenger passenger = passengerRepository.findById(passengerId);
        if (passenger == null) {
            throw new IllegalArgumentException("Passenger not found");
        }
        if (!passenger.getEmail().equalsIgnoreCase(email)) {
            throw new IllegalArgumentException("Email does not match passenger");
        }

        String normalizedEmail = passenger.getEmail().trim().toLowerCase(java.util.Locale.ROOT);

        String passwordHash = passwordService.hashPassword(password);

        return userRepository.save(passengerId, normalizedEmail, passwordHash);
    }

    // Retrieve an existing account
    public Optional<AppUser> findByEmail(String email) throws SQLException {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }

        return userRepository.findByEmail(email.trim().toLowerCase(java.util.Locale.ROOT));
    }

    // Check credentials for an existing account
    public Optional<AppUser> authenticate(String email, String password) throws SQLException {
        if (password == null) {
            return Optional.empty();
        }
        Optional<AppUser> user = findByEmail(email);

        if (user.isEmpty()) {
            return Optional.empty();
        }

        AppUser account = user.get();

        if (!account.enabled()) {
            return Optional.empty();
        }

        boolean validPassword = passwordService.verifyPassword(password, account.passwordHash());
        if (!validPassword) {
            return Optional.empty();
        }

        return Optional.of(account);
    }
}
