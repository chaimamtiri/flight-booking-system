package com.flightbooking;

import java.util.ArrayList;
import java.util.List;

public class BookingSystem {

    // Attributs
    private final List<Flight> flights;
    private final List<Booking> bookings;

    // Constructeur
    public BookingSystem() {
        this.flights = new ArrayList<>();
        this.bookings = new ArrayList<>();
    }

    // Ajouter un vol
    public void addFlight(Flight flight) {

        if (flight == null) {
            throw new IllegalArgumentException("Flight cannot be null");
        }

        // Vérifier si le numéro de vol existe déjà
        for (Flight f : flights) {
            if (f.getFlightNumber().equalsIgnoreCase(
                    flight.getFlightNumber())) {
                throw new IllegalArgumentException("Flight already exists");
            }
        }

        flights.add(flight);
    }

    // Récupérer tous les vols
    public List<Flight> getFlights() {
        return new ArrayList<>(flights);
    }

    // Rechercher un vol par son numéro
    public Flight findFlightByNumber(String flightNumber) {
        if (flightNumber == null || flightNumber.isBlank()) {
            throw new IllegalArgumentException("Flight number cannot be empty");
        }
        for (Flight flight : flights) {
            if (flight.getFlightNumber()
                    .equalsIgnoreCase(flightNumber)) {
                return flight;
            }
        }

        return null;
    }

    // Rechercher les vols selon le départ et la destination
    public List<Flight> searchFlights(
            String origin,
            String destination) {

        if (origin == null || origin.isBlank()
                || destination == null || destination.isBlank()) {
            throw new IllegalArgumentException(
                    "Origin and destination cannot be empty"
            );
        }

        List<Flight> results = new ArrayList<>();

        for (Flight flight : flights) {

            if (flight.getOrigin().equalsIgnoreCase(origin)
                    && flight.getDestination()
                    .equalsIgnoreCase(destination)) {

                results.add(flight);
            }
        }

        return results;
    }

    // Récupérer toutes les réservations
    public List<Booking> getBookings() {
        return new ArrayList<>(bookings);
    }
}