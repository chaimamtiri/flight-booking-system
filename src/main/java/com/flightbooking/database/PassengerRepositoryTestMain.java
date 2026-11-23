package com.flightbooking.database;

import com.flightbooking.Passenger;
import com.flightbooking.repository.PassengerRepository;

import java.sql.SQLException;

public class PassengerRepositoryTestMain {

    public static void main(String[] args) {
        PassengerRepository repository = new PassengerRepository();
        try {
            // Éviter un doublon lors des exécutions suivantes
            if (repository.findById("P003") == null) {
                Passenger passenger = new Passenger(
                        "P003",
                        "Lina",
                        "Ben Ali",
                        "lina@example.com"
                );
                repository.save(passenger);
                System.out.println("Passenger saved successfully!");
            }
            // Rechercher le passager
            Passenger found = repository.findById("P003");
            System.out.println("\n=== FOUND PASSENGER ===");
            System.out.println(found);
            // Afficher tous les passagers
            System.out.println("\n=== ALL PASSENGERS ===");
            for (Passenger p : repository.findAll()) {
                System.out.println(p);
            }
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
