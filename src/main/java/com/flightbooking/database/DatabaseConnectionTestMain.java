package com.flightbooking.database;

import com.flightbooking.Flight;
import com.flightbooking.repository.FlightRepository;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class DatabaseConnectionTestMain {

    public static void main(String[] args) {

        try {
            // 1. Tester la connexion PostgreSQL
            try (Connection connection = DatabaseConnection.getConnection()) {
                System.out.println("Connected to PostgreSQL successfully!");
                System.out.println("Database: " + connection.getCatalog());
            }
            // 2. Créer le repository
            FlightRepository repository = new FlightRepository();
            // 3. Récupérer tous les vols
            System.out.println("\n=== ALL FLIGHTS ===");
            List<Flight> flights = repository.findAll();
            for (Flight flight : flights) {
                System.out.println(
                        flight.getFlightNumber() + " : " + flight.getAvailableSeats() + " places disponibles");
            }
            // 4. Rechercher un vol par numéro
            System.out.println("\n=== SEARCH FLIGHT ===");
            Flight foundFlight = repository.findByNumber("BJ101");
            if (foundFlight != null) {
                System.out.println("Flight found: " + foundFlight);
            } else {
                System.out.println("Flight not found!");
            }
            // 5. Ajouter un nouveau vol dans PostgreSQL
            System.out.println("\n=== SAVE NEW FLIGHT ===");
            Flight existingFlight = repository.findByNumber("BJ303");
            if (existingFlight == null) {
                Flight newFlight = new Flight("BJ303", "Nice", "Paris", 95.0, 100);
                repository.save(newFlight);
                System.out.println("New flight saved successfully!");
            } else {
                System.out.println("Flight BJ303 already exists.");
            }
            // 6. Vérifier que le vol a été enregistré
            System.out.println("\n=== VERIFY SAVED FLIGHT ===");
            Flight savedFlight = repository.findByNumber("BJ303");
            if (savedFlight != null) {
                System.out.println("Saved flight: " + savedFlight);
            } else {
                System.out.println("Flight BJ303 not found!");
            }
            // 7. Afficher tous les vols après l'ajout
            System.out.println("\n=== UPDATED FLIGHT LIST ===");
            for (Flight flight : repository.findAll()) {
                System.out.println(flight);
            }
        } catch (SQLException | IllegalStateException e) {
            System.err.println("Database operation failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
