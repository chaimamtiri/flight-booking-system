package com.flightbooking.controller;

import com.flightbooking.Booking;
import com.flightbooking.Passenger;
import com.flightbooking.repository.BookingRepository;
import com.flightbooking.repository.PassengerRepository;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingRepository bookingRepository;
    private final PassengerRepository passengerRepository;

    public BookingController(BookingRepository bookingRepository, PassengerRepository passengerRepository) {
        this.bookingRepository = bookingRepository;
        this.passengerRepository = passengerRepository;
    }

    // Données reçues depuis le client
    public record BookingRequest(String bookingId, String passengerId, String flightNumber) {}

    // Réponse renvoyée au client
    public record BookingResponse(String bookingId, String passengerId, String flightNumber, String status) {}

    // POST /api/bookings
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse createBooking(@RequestBody BookingRequest request) {
        if (request == null || request.bookingId() == null || request.bookingId().isBlank() || request.passengerId() == null || request.passengerId().isBlank() || request.flightNumber() == null || request.flightNumber().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing required booking information");
        }
        try {
            Passenger passenger = passengerRepository.findById(request.passengerId());
            if (passenger == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Passenger not found");
            }

            Booking booking = bookingRepository.bookFlight(request.bookingId(), passenger, request.flightNumber());
            return new BookingResponse(booking.getBookingId(), booking.getPassenger().getPassengerId(), booking.getFlight().getFlightNumber(), booking.getStatus().name());
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Booking ID already exists");
            }
            if ("23503".equals(e.getSQLState())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Referenced passenger or flight not found");
            }

            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database operation failed");
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Flight not found or no available seats");
        }
    }

    // PATCH /api/bookings/B002/cancel
    @PatchMapping("/{bookingId}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelBooking(@PathVariable String bookingId) {
        try {
            boolean cancelled = bookingRepository.cancelBooking(bookingId);
            if (!cancelled) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Booking not found or already cancelled");
            }
        } catch (SQLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database operation failed"
            );
        }
    }
}
