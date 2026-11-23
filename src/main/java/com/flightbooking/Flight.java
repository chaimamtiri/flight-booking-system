package com.flightbooking;

public class Flight {
    private String flightNumber;
    private String origin;
    private String destination;
    private double price;
    private int availableSeats;

    public Flight (String flightNumber, String origin, String destination, double price, int availableSeats) {
        this.flightNumber = flightNumber;
        this.origin = origin;
        this.destination = destination;
        this.price = price;
        this.availableSeats = availableSeats;
    }

    //getters
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
    public int getAvailableSeats() {
        return availableSeats;
    }

    // methodes

    // savoir sil possede des places disponibles
     public boolean hasAvailableSeats() {
        return availableSeats > 0;
     }

    // reserver une place
     void reserveSeat() {
        if (hasAvailableSeats()) availableSeats--;
     }

     // affichage
    @Override
    public String toString() {
        return "Flight{" +
                "flightNumber='" + flightNumber + '\'' +
                ", origin='" + origin + '\'' +
                ", destination='" + destination + '\'' +
                ", price=" + price +
                ", availableSeats=" + availableSeats +
                '}';
    }
}


