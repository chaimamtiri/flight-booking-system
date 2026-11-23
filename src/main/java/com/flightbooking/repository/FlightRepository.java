package com.flightbooking.repository;

import com.flightbooking.Flight;
import com.flightbooking.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
                        result.getInt("total_seats")
                );
                flights.add(flight);
            }
        }
        return flights;
    }
}
