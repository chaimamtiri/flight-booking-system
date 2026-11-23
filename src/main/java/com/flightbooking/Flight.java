package com.flightbooking;

public class Flight {

    // Attributs
    private String flightNumber;
    private String origin;
    private String destination;
    private double price;
    private int totalSeats;
    private int availableSeats;

    // Constructeur
    public Flight(String flightNumber, String origin, String destination, double price, int totalSeats) {
        if (totalSeats < 0) {
            throw new IllegalArgumentException("Total seats cannot be negative");
        }
        if (price < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        this.flightNumber = flightNumber;
        this.origin = origin;
        this.destination = destination;
        this.price = price;
        this.totalSeats = totalSeats;
        this.availableSeats = totalSeats;
    }

    public Flight(String flightNumber, String origin, String destination, double price, int totalSeats, int availableSeats) {
        this(flightNumber, origin, destination, price, totalSeats);
        if (availableSeats < 0 || availableSeats > totalSeats) {
            throw new IllegalArgumentException("Invalid number of available seats");
        }
        this.availableSeats = availableSeats;
    }

    // Getters
    public String getFlightNumber() {
        return flightNumber;
    }

    public String getOrigin() {
        return origin;
    }

    public String getDestination() {
        return destination;
    }

    public double getPrice() {
        return price;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    // Vérifier si des places sont disponibles
    public boolean hasAvailableSeats() {
        return availableSeats > 0;
    }

    // Réserver une place
    public boolean reserveSeat() {
        if (hasAvailableSeats()) {
            availableSeats--;
            return true;
        }
        return false;
    }

    // Libérer une place
    public boolean releaseSeat() {
        if (availableSeats < totalSeats) {
            availableSeats++;
            return true;
        }
        return false;
    }

    // Affichage du vol
    @Override
    public String toString() {
        return "Flight{" +
                "flightNumber='" + flightNumber + '\'' +
                ", origin='" + origin + '\'' +
                ", destination='" + destination + '\'' +
                ", price=" + price +
                ", totalSeats=" + totalSeats +
                ", availableSeats=" + availableSeats +
                '}';
    }
}