package com.flightbooking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PassengerTest {

    private Passenger passenger;

    @BeforeEach
    void setUp() {
        passenger = new Passenger(
                "P001", "Sarah", "Martin", "sarah@example.com"
        );
    }

    @Test
    void testPassengerCreation() {
        assertEquals("P001", passenger.getPassengerId());
        assertEquals("Sarah", passenger.getFirstName());
        assertEquals("Martin", passenger.getLastName());
        assertEquals("sarah@example.com", passenger.getEmail());
    }

    @Test
    void testChangeEmail() {
        passenger.setEmail("new@example.com");

        assertEquals("new@example.com", passenger.getEmail());
    }

    @Test
    void testNullPassengerId() {
        assertThrows(IllegalArgumentException.class, () ->
                new Passenger(null, "Sarah", "Martin",
                        "sarah@example.com")
        );
    }

    @Test
    void testBlankPassengerId() {
        assertThrows(IllegalArgumentException.class, () ->
                new Passenger("  ", "Sarah", "Martin",
                        "sarah@example.com")
        );
    }

    @Test
    void testBlankFirstName() {
        assertThrows(IllegalArgumentException.class, () ->
                new Passenger("P002", " ", "Martin",
                        "sarah@example.com")
        );
    }

    @Test
    void testNullLastName() {
        assertThrows(IllegalArgumentException.class, () ->
                new Passenger("P002", "Sarah", null,
                        "sarah@example.com")
        );
    }

    @Test
    void testBlankLastName() {
        assertThrows(IllegalArgumentException.class, () ->
                new Passenger("P002", "Sarah", " ",
                        "sarah@example.com")
        );
    }

    @Test
    void testNullEmail() {
        assertThrows(IllegalArgumentException.class, () ->
                new Passenger("P002", "Sarah", "Martin", null)
        );
    }

    @Test
    void testBlankEmail() {
        assertThrows(IllegalArgumentException.class, () ->
                new Passenger("P002", "Sarah", "Martin", " ")
        );
    }

    @Test
    void testInvalidEmailUpdate() {
        assertThrows(IllegalArgumentException.class, () ->
                passenger.setEmail("  ")
        );

        assertEquals("sarah@example.com", passenger.getEmail());
    }

    @Test
    void testToString() {
        String result = passenger.toString();

        assertTrue(result.contains("P001"));
        assertTrue(result.contains("Sarah"));
        assertTrue(result.contains("Martin"));
    }
}
