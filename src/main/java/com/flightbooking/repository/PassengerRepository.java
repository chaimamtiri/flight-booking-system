package com.flightbooking.repository;

import com.flightbooking.Passenger;
import com.flightbooking.database.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PassengerRepository {

    // Enregistrer un passager
    public void save(Passenger passenger) throws SQLException {
        if (passenger == null) {throw new IllegalArgumentException("Passenger cannot be null");
        }
        String sql = """
                INSERT INTO passengers
                (passenger_id, first_name, last_name, email)
                VALUES (?, ?, ?, ?)
                """;
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, passenger.getPassengerId());
            statement.setString(2, passenger.getFirstName());
            statement.setString(3, passenger.getLastName());
            statement.setString(4, passenger.getEmail());
            statement.executeUpdate();
        }
    }
    // Rechercher un passager par identifiant
    public Passenger findById(String passengerId) throws SQLException {
        String sql = """
                SELECT *
                FROM passengers
                WHERE passenger_id = ?
                """;
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, passengerId);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return new Passenger(
                            result.getString("passenger_id"),
                            result.getString("first_name"),
                            result.getString("last_name"),
                            result.getString("email")
                    );
                }
            }
        }

        return null;
    }
    // Récupérer tous les passagers
    public List<Passenger> findAll() throws SQLException {
        List<Passenger> passengers = new ArrayList<>();
        String sql = "SELECT * FROM passengers";
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                Passenger passenger = new Passenger(
                        result.getString("passenger_id"),
                        result.getString("first_name"),
                        result.getString("last_name"),
                        result.getString("email")
                );
                passengers.add(passenger);
            }
        }
        return passengers;
    }
}
