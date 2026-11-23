package com.flightbooking.database;

import com.flightbooking.Booking;
import com.flightbooking.Passenger;
import com.flightbooking.repository.BookingRepository;
import com.flightbooking.repository.FlightRepository;
import com.flightbooking.repository.PassengerRepository;

import java.sql.SQLException;

public class BookingRepositoryTestMain {

    public static void main(String[] args) {
        PassengerRepository passengerRepository = new PassengerRepository();
        BookingRepository bookingRepository = new BookingRepository();
        FlightRepository flightRepository = new FlightRepository();
        try {
            Passenger passenger = passengerRepository.findById("P003");
            if (passenger == null) {
                System.out.println("Passenger P003 not found");
                return;
            }

            System.out.println("Before booking: " + flightRepository.findByNumber("BJ101").getAvailableSeats());
            Booking booking = bookingRepository.bookFlight("B002", passenger, "BJ101");
            System.out.println("Booking created: " + booking.getBookingId());
            System.out.println("Status: " + booking.getStatus());
            System.out.println("After booking: " + flightRepository.findByNumber("BJ101").getAvailableSeats());
        } catch (SQLException | RuntimeException e) {
            System.err.println("Booking failed: " + e.getMessage());
        }
    }
}
