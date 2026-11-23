package com.flightbooking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FlightTest {

    private Flight flight;

    @BeforeEach
    void setUp() {
        flight = new Flight(
                "BJ101", "Nice", "Tunis", 120.0, 2
        );
    }

    @Test
    void testFlightCreation() {
        assertEquals("BJ101", flight.getFlightNumber());
        assertEquals("Nice", flight.getOrigin());
        assertEquals("Tunis", flight.getDestination());
        assertEquals(120.0, flight.getPrice());
        assertEquals(2, flight.getTotalSeats());
        assertEquals(2, flight.getAvailableSeats());
    }

    @Test
    void testHasAvailableSeats() {
        assertTrue(flight.hasAvailableSeats());
    }

    @Test
    void testReserveSeat() {
        assertTrue(flight.reserveSeat());
        assertEquals(1, flight.getAvailableSeats());
    }

    @Test
    void testReserveLastSeat() {
        flight.reserveSeat();

        assertTrue(flight.reserveSeat());
        assertEquals(0, flight.getAvailableSeats());
        assertFalse(flight.hasAvailableSeats());
    }

    @Test
    void testReserveSeatWhenFull() {
        flight.reserveSeat();
        flight.reserveSeat();

        assertFalse(flight.reserveSeat());
        assertEquals(0, flight.getAvailableSeats());
    }

    @Test
    void testReleaseSeat() {
        flight.reserveSeat();

        assertTrue(flight.releaseSeat());
        assertEquals(2, flight.getAvailableSeats());
    }

    @Test
    void testReleaseSeatWhenAlreadyFull() {
        assertFalse(flight.releaseSeat());
        assertEquals(2, flight.getAvailableSeats());
    }

    @Test
    void testNegativePrice() {
        assertThrows(IllegalArgumentException.class, () ->
                new Flight("BJ102", "Nice", "Paris", -50, 100)
        );
    }

    @Test
    void testNegativeSeats() {
        assertThrows(IllegalArgumentException.class, () ->
                new Flight("BJ103", "Nice", "Tunis", 120, -1)
        );
    }

    @Test
    void testZeroSeats() {
        Flight emptyFlight = new Flight(
                "BJ104", "Nice", "Tunis", 120, 0
        );

        assertFalse(emptyFlight.hasAvailableSeats());
        assertFalse(emptyFlight.reserveSeat());
    }

    @Test
    void testToString() {
        String result = flight.toString();

        assertTrue(result.contains("BJ101"));
        assertTrue(result.contains("Nice"));
        assertTrue(result.contains("Tunis"));
    }
}
