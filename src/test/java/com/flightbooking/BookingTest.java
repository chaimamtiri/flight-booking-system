package com.flightbooking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BookingTest {

    private Flight flight;
    private Passenger passenger;
    private Booking booking;

    @BeforeEach
    void setUp() {
        flight = new Flight(
                "BJ101", "Nice", "Tunis", 120.0, 2
        );

        passenger = new Passenger(
                "P001", "Sarah", "Martin", "sarah@example.com"
        );

        // Le constructeur Booking ne réserve pas
        // automatiquement une place.
        flight.reserveSeat();

        booking = new Booking("B001", passenger, flight);
    }

    @Test
    void testBookingCreation() {
        assertEquals("B001", booking.getBookingId());
        assertSame(passenger, booking.getPassenger());
        assertSame(flight, booking.getFlight());
    }

    @Test
    void testInitialStatus() {
        assertEquals(BookingStatus.CONFIRMED,
                booking.getStatus());
    }

    @Test
    void testCancelBooking() {
        assertTrue(booking.cancel());

        assertEquals(BookingStatus.CANCELLED,
                booking.getStatus());
    }

    @Test
    void testCancellationReleasesSeat() {
        assertEquals(1, flight.getAvailableSeats());

        booking.cancel();

        assertEquals(2, flight.getAvailableSeats());
    }

    @Test
    void testCannotCancelTwice() {
        assertTrue(booking.cancel());
        assertFalse(booking.cancel());

        assertEquals(2, flight.getAvailableSeats());
    }

    @Test
    void testCancelWhenSeatCannotBeReleased() {
        // Simule un état incohérent : la place a
        // déjà été libérée en dehors de Booking.
        flight.releaseSeat();

        assertFalse(booking.cancel());
        assertEquals(BookingStatus.CONFIRMED,
                booking.getStatus());
    }

    @Test
    void testNullPassenger() {
        assertThrows(IllegalArgumentException.class, () ->
                new Booking("B002", null, flight)
        );
    }

    @Test
    void testNullFlight() {
        assertThrows(IllegalArgumentException.class, () ->
                new Booking("B002", passenger, null)
        );
    }

    @Test
    void testBlankBookingId() {
        assertThrows(IllegalArgumentException.class, () ->
                new Booking("  ", passenger, flight)
        );
    }

    @Test
    void testNullBookingId() {
        assertThrows(IllegalArgumentException.class, () ->
                new Booking(null, passenger, flight)
        );
    }

    @Test
    void testToString() {
        String result = booking.toString();

        assertTrue(result.contains("B001"));
        assertTrue(result.contains("CONFIRMED"));
    }
}
