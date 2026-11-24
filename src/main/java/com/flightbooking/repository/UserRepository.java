package com.flightbooking.repository;

import com.flightbooking.database.DatabaseConnection;
import com.flightbooking.model.AppUser;

import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Repository
public class UserRepository {

    // Save a new account with an already hashed password
    public AppUser save(String passengerId, String email, String passwordHash) throws SQLException {
        String sql = """
                INSERT INTO app_users
                    (passenger_id, email, password_hash)
                VALUES (?, ?, ?)
                RETURNING user_id, enabled
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, passengerId);
            statement.setString(2, email);
            statement.setString(3, passwordHash);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new SQLException("Unable to create user");
                }

                return new AppUser(result.getLong("user_id"), passengerId, email, passwordHash, result.getBoolean("enabled"));
            }
        }
    }

    // Find an account using its email
    public Optional<AppUser> findByEmail(String email) throws SQLException {
        String sql = """
                SELECT user_id, passenger_id,
                       email, password_hash, enabled
                FROM app_users
                WHERE LOWER(email) = LOWER(?)
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return Optional.empty();
                }
                AppUser user = new AppUser(
                        result.getLong("user_id"),
                        result.getString("passenger_id"),
                        result.getString("email"),
                        result.getString("password_hash"),
                        result.getBoolean("enabled")
                );
                return Optional.of(user);
            }
        }
    }
}
