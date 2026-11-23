package com.flightbooking;

public class Main {
    public static void main(String[] args) {

        Flight flight = new Flight(
                "BJ101", "Nice", "Tunis", 120.0, 2
        );

        Passenger passenger = new Passenger(
                "P001", "Sarah", "Martin", "sarah@example.com"
        );

        // Réserver une place avant de créer Booking
        flight.reserveSeat();

        Booking booking = new Booking("B001", passenger, flight);

        System.out.println("Before cancellation: "
                + flight.getAvailableSeats());

        System.out.println("First cancellation: "
                + booking.cancel());

        System.out.println("After cancellation: "
                + flight.getAvailableSeats());

        System.out.println("Second cancellation: "
                + booking.cancel());
    }
}