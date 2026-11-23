
package com.flightbooking.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    public static Connection getConnection() throws SQLException {
        String url = System.getenv("DB_URL");
        if (url == null || url.isBlank()) {
            url = "jdbc:postgresql://localhost:5432/flight_booking_db";
        }
        String password = System.getenv("DB_PASSWORD");
        if (password == null || password.isBlank()) {
            throw new IllegalStateException("DB_PASSWORD environment variable is missing");
        }
        return DriverManager.getConnection(url, "postgres", password);
    }
}
