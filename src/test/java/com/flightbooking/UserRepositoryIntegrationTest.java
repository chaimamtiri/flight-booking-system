package com.flightbooking;

import com.flightbooking.database.DatabaseConnection;
import com.flightbooking.model.AppUser;
import com.flightbooking.repository.UserRepository;
import com.flightbooking.security.PasswordService;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class UserRepositoryIntegrationTest {

    @Test
    void shouldSaveAndFindUser() throws Exception {

        String testUrl =
                "jdbc:postgresql://localhost:5432/flight_booking_test_db";

        assumeTrue(testUrl.equals(System.getenv("DB_URL")),
                "Requires the dedicated PostgreSQL test database");

        UserRepository repository = new UserRepository();
        PasswordService passwordService = new PasswordService();

        String passengerId = "U" +
                UUID.randomUUID().toString().substring(0, 12);

        String email = passengerId.toLowerCase() + "@example.com";

        // Create tables in the TEST database
        try (Connection connection =
                     DatabaseConnection.getConnection();
             Statement statement = connection.createStatement()) {

            statement.execute("""
                    CREATE TABLE IF NOT EXISTS passengers (
                        passenger_id VARCHAR(20) PRIMARY KEY,
                        first_name VARCHAR(100) NOT NULL,
                        last_name VARCHAR(100) NOT NULL,
                        email VARCHAR(255) NOT NULL
                    )
                    """);

            statement.execute("""
                    CREATE TABLE IF NOT EXISTS app_users (
                        user_id BIGINT GENERATED ALWAYS AS IDENTITY
                            PRIMARY KEY,
                        passenger_id VARCHAR(20) NOT NULL UNIQUE
                            REFERENCES passengers(passenger_id),
                        email VARCHAR(255) NOT NULL UNIQUE,
                        password_hash VARCHAR(255) NOT NULL,
                        enabled BOOLEAN NOT NULL DEFAULT TRUE
                    )
                    """);

            try (PreparedStatement insert = connection.prepareStatement(
                    """
                    INSERT INTO passengers
                        (passenger_id, first_name, last_name, email)
                    VALUES (?, 'Test', 'User', ?)
                    """)) {

                insert.setString(1, passengerId);
                insert.setString(2, email);
                insert.executeUpdate();
            }
        }

        try {
            String rawPassword = "ExamplePassword!2026";
            String hash = passwordService.hashPassword(rawPassword);

            AppUser saved = repository.save(
                    passengerId, email, hash
            );

            assertTrue(saved.userId() > 0);
            assertEquals(passengerId, saved.passengerId());
            assertNotEquals(rawPassword, saved.passwordHash());

            Optional<AppUser> found =
                    repository.findByEmail(email);

            assertTrue(found.isPresent());
            assertEquals(saved.userId(), found.get().userId());

            assertTrue(passwordService.verifyPassword(
                    rawPassword, found.get().passwordHash()
            ));

            assertFalse(repository.findByEmail(
                    "unknown-" + passengerId + "@example.com"
            ).isPresent());

        } finally {
            // Remove only our generated test data
            try (Connection connection =
                         DatabaseConnection.getConnection()) {

                try (PreparedStatement delete = connection.prepareStatement(
                        "DELETE FROM app_users WHERE passenger_id = ?")) {
                    delete.setString(1, passengerId);
                    delete.executeUpdate();
                }

                try (PreparedStatement delete = connection.prepareStatement(
                        "DELETE FROM passengers WHERE passenger_id = ?")) {
                    delete.setString(1, passengerId);
                    delete.executeUpdate();
                }
            }
        }
    }
}
