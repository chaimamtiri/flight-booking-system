package com.flightbooking;

public class Booking {
    private final String bookingId ;
    private final Passenger passenger;
    private final Flight flight;
    private BookingStatus status;

    public Booking(String bookingId, Passenger passenger, Flight flight) {
        if (bookingId == null || bookingId.isBlank()) {
            throw new IllegalArgumentException("bookingId cannot be null or blank");
        }
        if (passenger == null) {
            throw new IllegalArgumentException("passenger cannot be null");
        }
        if (flight == null) {
            throw new IllegalArgumentException("flight cannot be null");
        }
        this.bookingId = bookingId;
        this.passenger = passenger;
        this.flight = flight;
        this.status = BookingStatus.CONFIRMED;
    }

    // Getters
    public String getBookingId() {
        return bookingId;
    }
    public Passenger getPassenger() {
        return passenger;
    }
    public Flight getFlight() {
        return flight;
    }
    public BookingStatus getStatus() {
        return status;
    }

    public boolean cancel() {
        if (status == BookingStatus.CANCELLED) {
            return false;
        }
        if (!flight.releaseSeat()) {
            return false;
        }
        status = BookingStatus.CANCELLED;
        return true;
    }

    @Override
    public String toString() {
        return "Booking{" +
                "bookingId='" + bookingId + '\'' +
                ", passenger=" + passenger +
                ", flight=" + flight +
                ", status=" + status +
                '}';
    }
}


