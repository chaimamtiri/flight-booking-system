package com.flightbooking;

public class Main {

    public static void main(String[] args) {

        Flight flight = new Flight(
                "BJ101", "Nice", "Tunis", 120.0, 2
        );

        System.out.println(flight);

        // Réserver 3 fois
        System.out.println(flight.reserveSeat());
        System.out.println(flight.reserveSeat());
        System.out.println(flight.reserveSeat());

        System.out.println("Available seats: "
                + flight.getAvailableSeats());

        // Libérer 3 fois
        System.out.println(flight.releaseSeat());
        System.out.println(flight.releaseSeat());
        System.out.println(flight.releaseSeat());

        System.out.println("Available seats: "
                + flight.getAvailableSeats());
    }
}