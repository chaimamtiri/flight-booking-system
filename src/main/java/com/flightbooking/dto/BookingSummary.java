package com.flightbooking.dto;

public record BookingSummary(
        String bookingId,
        String passengerId,
        String flightNumber,
        String status
) {}
