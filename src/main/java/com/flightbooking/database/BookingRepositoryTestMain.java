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
            System.out.println("Before cancellation: " + flightRepository.findByNumber("BJ101").getAvailableSeats());
            System.out.println("First cancellation: " + bookingRepository.cancelBooking("B002"));
            System.out.println("After cancellation: " + flightRepository.findByNumber("BJ101").getAvailableSeats());
            System.out.println("Second cancellation: " + bookingRepository.cancelBooking("B002"));
        } catch (SQLException | RuntimeException e) {
            System.err.println("Booking failed: " + e.getMessage());
        }


    }
}
