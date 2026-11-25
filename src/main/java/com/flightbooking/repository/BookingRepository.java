package com.flightbooking.repository;

import com.flightbooking.Booking;
import com.flightbooking.Flight;
import com.flightbooking.Passenger;
import com.flightbooking.database.DatabaseConnection;

import com.flightbooking.dto.BookingSummary;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
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

    public boolean cancelBooking(String bookingId) throws SQLException {
        if (bookingId == null || bookingId.isBlank()) {
            throw new IllegalArgumentException("Booking ID cannot be empty");
        }
        String cancelSql = """
            UPDATE bookings
            SET status = 'CANCELLED'
            WHERE booking_id = ?
              AND status = 'CONFIRMED'
            RETURNING flight_number
            """;
        String releaseSql = """
            UPDATE flights
            SET available_seats = available_seats + 1
            WHERE flight_number = ?
              AND available_seats < total_seats
            """;
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                String flightNumber;
                // 1. Annuler la réservation
                try (PreparedStatement statement = connection.prepareStatement(cancelSql)) {
                    statement.setString(1, bookingId);
                    try (ResultSet result = statement.executeQuery()) {
                        if (!result.next()) {
                            connection.rollback();
                            return false;
                        }
                        flightNumber = result.getString("flight_number");
                    }
                }
                // 2. Libérer une place
                try (PreparedStatement statement = connection.prepareStatement(releaseSql)) {
                    statement.setString(1, flightNumber);
                    int updatedRows = statement.executeUpdate();
                    if (updatedRows != 1) {
                        throw new IllegalStateException("Unable to release seat");
                    }
                }
                // 3. Valider les deux modifications
                connection.commit();
                return true;
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


    public List<BookingSummary> findByPassengerId(
            String passengerId) throws SQLException {

        String sql = """
            SELECT booking_id, passenger_id,
                   flight_number, status
            FROM bookings
            WHERE passenger_id = ?
            ORDER BY booking_id
            """;

        List<BookingSummary> bookings = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, passengerId);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    bookings.add(new BookingSummary(
                            result.getString("booking_id"),
                            result.getString("passenger_id"),
                            result.getString("flight_number"),
                            result.getString("status")
                    ));
                }
            }
        }

        return bookings;
    }


    public boolean cancelBookingForPassenger(String bookingId, String passengerId) throws SQLException {
        String cancelSql = """
            UPDATE bookings
            SET status = 'CANCELLED'
            WHERE booking_id = ?
              AND passenger_id = ?
              AND status = 'CONFIRMED'
            RETURNING flight_number
            """;

        String releaseSeatSql = """
            UPDATE flights
            SET available_seats = available_seats + 1
            WHERE flight_number = ?
              AND available_seats < total_seats
            """;

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                String flightNumber;
                try (PreparedStatement statement = connection.prepareStatement(cancelSql)) {
                    statement.setString(1, bookingId);
                    statement.setString(2, passengerId);
                    try (ResultSet result = statement.executeQuery()) {
                        if (!result.next()) {connection.rollback();
                            return false;
                        }
                        flightNumber = result.getString("flight_number");
                    }
                }
                int updatedSeats;
                try (PreparedStatement statement = connection.prepareStatement(releaseSeatSql)) {
                    statement.setString(1, flightNumber);
                    updatedSeats = statement.executeUpdate();
                }

                if (updatedSeats != 1) {
                    throw new SQLException("Unable to release the reserved seat");
                }
                connection.commit();
                return true;
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            }
        }
    }



}
