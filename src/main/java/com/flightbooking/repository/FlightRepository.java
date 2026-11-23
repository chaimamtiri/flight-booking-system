package com.flightbooking.repository;

import org.springframework.stereotype.Repository;
import com.flightbooking.Flight;
import com.flightbooking.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class FlightRepository {

    public List<Flight> findAll() throws SQLException {
        List<Flight> flights = new ArrayList<>();
        String sql = "SELECT * FROM flights";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                Flight flight = new Flight(
                        result.getString("flight_number"),
                        result.getString("origin"),
                        result.getString("destination"),
                        result.getDouble("price"),
                        result.getInt("total_seats"),
                        result.getInt("available_seats")
                );
                flights.add(flight);
            }
        }
        return flights;
    }


    public Flight findByNumber(String flightNumber) throws SQLException {
        String sql = """
            SELECT flight_number, origin, destination,
                   price, total_seats, available_seats
            FROM flights
            WHERE flight_number = ?
            """;
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, flightNumber);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return new Flight(
                            result.getString("flight_number"),
                            result.getString("origin"),
                            result.getString("destination"),
                            result.getDouble("price"),
                            result.getInt("total_seats"),
                            result.getInt("available_seats")
                    );
                }
            }
        }

        return null;
    }


    public void save(Flight flight) throws SQLException {
        if (flight == null) {
            throw new IllegalArgumentException("Flight cannot be null");
        }
        String sql = """
            INSERT INTO flights (
                flight_number, origin, destination,
                price, total_seats, available_seats
            )
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, flight.getFlightNumber());
            statement.setString(2, flight.getOrigin());
            statement.setString(3, flight.getDestination());
            statement.setDouble(4, flight.getPrice());
            statement.setInt(5, flight.getTotalSeats());
            statement.setInt(6, flight.getAvailableSeats());
            statement.executeUpdate();
        }
    }


}
