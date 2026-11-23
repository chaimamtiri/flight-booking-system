package com.flightbooking;

public class Main {

    public static void main(String[] args) {

        BookingSystem system = new BookingSystem();

        Flight flight1 = new Flight(
                "BJ101", "Nice", "Tunis", 120.0, 180
        );

        Flight flight2 = new Flight(
                "AF202", "Paris", "Nice", 90.0, 150
        );

        Flight flight3 = new Flight(
                "BJ303", "Nice", "Tunis", 150.0, 100
        );

        // Ajouter les vols
        system.addFlight(flight1);
        system.addFlight(flight2);
        system.addFlight(flight3);

        // Afficher tous les vols
        System.out.println("All flights:");
        for (Flight flight : system.getFlights()) {
            System.out.println(flight);
        }

        // Rechercher Nice → Tunis
        System.out.println("\nFlights from Nice to Tunis:");

        for (Flight flight : system.searchFlights("Nice", "Tunis")) {
            System.out.println(flight);
        }

        // Rechercher un vol par numéro
        System.out.println("\nSearch flight BJ101:");
        System.out.println(system.findFlightByNumber("BJ101"));
    }
}