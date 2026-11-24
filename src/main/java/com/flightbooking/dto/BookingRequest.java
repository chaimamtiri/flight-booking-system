package com.flightbooking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BookingRequest(

        @NotBlank(message = "Booking ID is required")
        @Size(max = 20)
        String bookingId,

        @NotBlank(message = "Passenger ID is required")
        @Size(max = 20)
        String passengerId,

        @NotBlank(message = "Flight number is required")
        @Size(max = 20)
        String flightNumber

) {}
