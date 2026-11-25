package com.flightbooking.controller;

import com.flightbooking.Booking;
import com.flightbooking.Passenger;
import com.flightbooking.dto.ApiError;
import com.flightbooking.dto.BookingRequest;
import com.flightbooking.repository.BookingRepository;
import com.flightbooking.repository.PassengerRepository;
import com.flightbooking.dto.BookingSummary;
import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.flightbooking.model.AppUser;
import com.flightbooking.service.UserService;
import org.springframework.security.core.Authentication;
import java.util.List;

import com.flightbooking.model.AppUser;
import org.springframework.security.core.Authentication;


import com.flightbooking.dto.BookingSummary;
import java.util.List;

import java.sql.SQLException;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingRepository bookingRepository;
    private final PassengerRepository passengerRepository;
    private final UserService userService;

    public BookingController(
            BookingRepository bookingRepository,
            PassengerRepository passengerRepository,
            UserService userService) {

        this.bookingRepository = bookingRepository;
        this.passengerRepository = passengerRepository;
        this.userService = userService;
    }

    public record BookingResponse(String bookingId, String passengerId, String flightNumber, String status
    ) {}


    @Operation(
            summary = "Retrieve bookings for a passenger",
            description = "Returns bookings associated with a passenger ID"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Bookings retrieved successfully"
    )
    @ApiResponse(
            responseCode = "400",
            description = "Passenger ID is required",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiError.class)
            )
    )
    @GetMapping
    public List<BookingSummary> getBookings(
            @RequestParam String passengerId) {

        if (passengerId.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Passenger ID is required"
            );
        }

        try {
            return bookingRepository.findByPassengerId(passengerId);

        } catch (SQLException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Database operation failed"
            );
        }
    }


    // POST /api/bookings
    @Operation(
            summary = "Create a flight booking",
            description = "Creates a confirmed booking for an existing " +
                    "passenger and decreases available seats by one"
    )
    @ApiResponse(
            responseCode = "201",
            description = "Booking created successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(
                            implementation = BookingResponse.class
                    )
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Invalid booking request",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiError.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Passenger not found",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiError.class)
            )
    )
    @ApiResponse(
            responseCode = "409",
            description = "Duplicate booking, unavailable flight or no seats",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiError.class)
            )
    )
    @ApiResponse(
            responseCode = "500",
            description = "Database operation failed",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiError.class)
            )
    )

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse createBooking(
            @Valid @RequestBody BookingRequest request,
            Authentication authentication) {

        try {
            AppUser user = getAuthenticatedUser(authentication);

            // Refuse booking for another passenger
            if (!user.passengerId().equals(
                    request.passengerId())) {

                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Cannot book for another passenger"
                );
            }

            Passenger passenger =
                    passengerRepository.findById(
                            user.passengerId()
                    );

            if (passenger == null) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Passenger not found"
                );
            }

            Booking booking = bookingRepository.bookFlight(
                    request.bookingId(),
                    passenger,
                    request.flightNumber()
            );

            return new BookingResponse(
                    booking.getBookingId(),
                    booking.getPassenger().getPassengerId(),
                    booking.getFlight().getFlightNumber(),
                    booking.getStatus().name()
            );

        } catch (SQLException e) {

            if ("23505".equals(e.getSQLState())) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Booking ID already exists"
                );
            }

            if ("23503".equals(e.getSQLState())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Referenced passenger or flight not found"
                );
            }

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Database operation failed"
            );

        } catch (IllegalStateException e) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Flight not found or no available seats"
            );
        }
    }


    // PATCH /api/bookings/{bookingId}/cancel
    @Operation(
            summary = "Cancel an existing booking",
            description = "Cancels a confirmed booking and releases one seat. " + "A booking cannot be cancelled twice."
    )
    @ApiResponse(
            responseCode = "204",
            description = "Booking cancelled successfully",
            content = @Content
    )
    @ApiResponse(
            responseCode = "409",
            description = "Booking not found or already cancelled",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
    )
    @ApiResponse(
            responseCode = "500",
            description = "Database operation failed",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
    )

    @PatchMapping("/{bookingId}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelBooking(@PathVariable String bookingId, Authentication authentication) {
        try {
            AppUser user = getAuthenticatedUser(authentication);
            boolean cancelled = bookingRepository.cancelBookingForPassenger(bookingId, user.passengerId());

            if (!cancelled) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Booking unavailable or already cancelled"
                );
            }

        } catch (SQLException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Database operation failed"
            );
        }
    }



    @Operation(
            summary = "Get my bookings",
            description = "Returns bookings belonging to the authenticated passenger"
    )
    @GetMapping("/me")
    public List<BookingSummary> getMyBookings(
            Authentication authentication) {

        if (authentication == null ||
                !authentication.isAuthenticated()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication required"
            );
        }

        try {
            AppUser user = userService
                    .findByEmail(authentication.getName())
                    .orElseThrow(() ->
                            new ResponseStatusException(
                                    HttpStatus.FORBIDDEN,
                                    "Account not found"
                            )
                    );

            if (!user.enabled()) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Account disabled"
                );
            }

            return bookingRepository.findByPassengerId(
                    user.passengerId()
            );

        } catch (SQLException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Database operation failed"
            );
        }
    }


    private AppUser getAuthenticatedUser(Authentication authentication) throws SQLException {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        AppUser user = userService
                .findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account not found"));

        if (!user.enabled()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account disabled");
        }

        return user;
    }


}
