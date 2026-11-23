
package com.flightbooking.database;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnectionTestMain {

    public static void main(String[] args) {

        try (Connection connection = DatabaseConnection.getConnection()) {
            System.out.println("Connected to PostgreSQL successfully!");
            System.out.println("Database: " + connection.getCatalog());
        } catch (SQLException | IllegalStateException e) {
            System.err.println("Database connection failed: " + e.getMessage());
        }
    }
}
