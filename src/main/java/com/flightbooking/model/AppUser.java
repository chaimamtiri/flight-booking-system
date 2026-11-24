package com.flightbooking.model;

public record AppUser(
        long userId,
        String passengerId,
        String email,
        String passwordHash,
        boolean enabled
) {}
