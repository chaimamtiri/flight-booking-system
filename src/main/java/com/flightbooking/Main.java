
package com.flightbooking;

public class Main {

    public static void main(String[] args) {

        BookingSystem system = new BookingSystem();

        // Créer un vol avec 2 places
        Flight flight = new Flight(
                "BJ101", "Nice", "Tunis", 120.0, 2);

        // Créer deux passagers
        Passenger p1 = new Passenger(
                "P001", "Sarah", "Martin", "sarah@example.com");

        Passenger p2 = new Passenger(
                "P002", "Adam", "Ali", "adam@example.com");

        // Ajouter le vol
        system.addFlight(flight);

        // Réserver deux places
        Booking b1 = system.bookFlight("B001", p1, "BJ101");
        Booking b2 = system.bookFlight("B002", p2, "BJ101");

        System.out.println("Booking 1: " + b1.getStatus());
        System.out.println("Booking 2: " + b2.getStatus());

        System.out.println("Available seats: "
                + flight.getAvailableSeats());

        // Tester une réservation lorsque le vol est complet
        try {
            system.bookFlight("B003", p1, "BJ101");
        } catch (IllegalStateException e) {
            System.out.println("Booking failed: " + e.getMessage());
        }

        // Annuler la première réservation
        System.out.println("Cancellation: " + system.cancelBooking("B001"));

        System.out.println("Available seats after cancellation: " + flight.getAvailableSeats());

        // Tester une deuxième annulation
        System.out.println("Second cancellation: " + system.cancelBooking("B001"));
    }
}
