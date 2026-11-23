
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
            if (flight.getFlightNumber().equalsIgnoreCase(flightNumber)) {
                return flight;
            }
        }

        return null;
    }

    // Rechercher les vols par départ et destination
    public List<Flight> searchFlights(String origin, String destination) {
        if (origin == null || origin.isBlank() || destination == null || destination.isBlank()) {
            throw new IllegalArgumentException("Origin and destination cannot be empty");
        }

        List<Flight> results = new ArrayList<>();

        for (Flight flight : flights) {
            if (flight.getOrigin().equalsIgnoreCase(origin) && flight.getDestination().equalsIgnoreCase(destination)) {
                results.add(flight);
            }
        }

        return results;
    }

    // Récupérer toutes les réservations
    public List<Booking> getBookings() {
        return new ArrayList<>(bookings);
    }

    // Rechercher une réservation par son identifiant
    public Booking findBookingById(String bookingId) {

        if (bookingId == null || bookingId.isBlank()) {
            throw new IllegalArgumentException("Booking ID cannot be empty");
        }

        for (Booking booking : bookings) {
            if (booking.getBookingId().equalsIgnoreCase(bookingId)) {
                return booking;
            }
        }

        return null;
    }

    // Réserver un vol
    public Booking bookFlight(String bookingId, Passenger passenger, String flightNumber) {

        // 1. Vérifier l'identifiant de réservation
        if (bookingId == null || bookingId.isBlank()) {
            throw new IllegalArgumentException("Booking ID cannot be empty");
        }

        // 2. Vérifier le passager
        if (passenger == null) {
            throw new IllegalArgumentException("Passenger cannot be null");
        }

        // 3. Rechercher le vol
        Flight flight = findFlightByNumber(flightNumber);

        if (flight == null) {
            throw new IllegalArgumentException("Flight not found");
        }

        // 4. Vérifier que la réservation n'existe pas déjà
        if (findBookingById(bookingId) != null) {
            throw new IllegalArgumentException("Booking ID already exists");
        }

        // 5. Réserver une place
        if (!flight.reserveSeat()) {
            throw new IllegalStateException("No available seats on this flight");
        }

        // 6. Créer la réservation
        Booking booking;

        try {
            booking = new Booking(bookingId, passenger, flight);
            bookings.add(booking);
        } catch (RuntimeException e) {
            // Restaurer la place si la création échoue
            flight.releaseSeat();
            throw e;
        }

        // 7. Retourner la réservation
        return booking;
    }

    // Annuler une réservation
    public boolean cancelBooking(String bookingId) {

        // 1. Rechercher la réservation
        Booking booking = findBookingById(bookingId);

        if (booking == null) {
            return false;
        }

        // 2. Annuler et libérer une place
        return booking.cancel();
    }
}
