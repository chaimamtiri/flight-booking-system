package com.flightbooking;

public class Main {
    public static void main(String[] args) {

        Flight flight = new Flight(
                "BJ101", "Nice", "Tunis", 120.0, 2
        );

        System.out.println(flight);

        System.out.println(
                "Seats available? " + flight.hasAvailableSeats()
        );

        System.out.println(
                "First reservation: " + flight.reserveSeat()
        );

        System.out.println(
                "Second reservation: " + flight.reserveSeat()
        );

        System.out.println(
                "Third reservation: " + flight.reserveSeat()
        );

        System.out.println(flight);
    }
}