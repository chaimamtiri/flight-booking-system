package com.flightbooking.repository;

import com.flightbooking.Booking;
import com.flightbooking.Flight;
import com.flightbooking.Passenger;
import com.flightbooking.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BookingRepository {

    public Booking bookFlight(String bookingId, Passenger passenger, String flightNumber) throws SQLException {
        if (bookingId == null || bookingId.isBlank()) {
            throw new IllegalArgumentException("Booking ID cannot be empty");
        }
        if (passenger == null) {throw new IllegalArgumentException(
                "Passenger cannot be null");
        }
        if (flightNumber == null || flightNumber.isBlank()) {
            throw new IllegalArgumentException("Flight number cannot be empty");
        }
        String updateSql = """
                UPDATE flights
                SET available_seats = available_seats - 1
                WHERE flight_number = ?
                  AND available_seats > 0
                """;
        String insertSql = """
                INSERT INTO bookings
                (booking_id, passenger_id, flight_number, status)
                VALUES (?, ?, ?, 'CONFIRMED')
                """;
        String selectSql = """
                SELECT flight_number, origin, destination,
                       price, total_seats, available_seats
                FROM flights
                WHERE flight_number = ?
                """;
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                // 1. Réserver une place dans PostgreSQL
                try (PreparedStatement statement = connection.prepareStatement(updateSql)) {
                    statement.setString(1, flightNumber);
                    int updatedRows = statement.executeUpdate();
                    if (updatedRows != 1) {
                        throw new IllegalStateException("Flight not found or no available seats");
                    }
                }
                // 2. Enregistrer la réservation
                try (PreparedStatement statement = connection.prepareStatement(insertSql)) {
                    statement.setString(1, bookingId);
                    statement.setString(2, passenger.getPassengerId());
                    statement.setString(3, flightNumber);
                    statement.executeUpdate();
                }
                // 3. Récupérer l'état actualisé du vol
                Flight flight;
                try (PreparedStatement statement = connection.prepareStatement(selectSql)) {
                    statement.setString(1, flightNumber);
                    try (ResultSet result = statement.executeQuery()) {
                        if (!result.next()) {
                            throw new SQLException("Flight disappeared during booking");
                        }
                        flight = new Flight(
                                result.getString("flight_number"),
                                result.getString("origin"),
                                result.getString("destination"),
                                result.getDouble("price"),
                                result.getInt("total_seats"),
                                result.getInt("available_seats")
                        );
                    }
                }
                Booking booking = new Booking(bookingId, passenger, flight);
                // 4. Valider toutes les opérations
                connection.commit();
                return booking;
            } catch (SQLException | RuntimeException e) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                throw e;
            }
        }
    }
}
