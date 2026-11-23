package com.flightbooking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BookingSystemTest {

    private BookingSystem system;
    private Flight flight;
    private Passenger passenger;

    @BeforeEach
    void setUp() {
        system = new BookingSystem();

        flight = new Flight(
                "BJ101", "Nice", "Tunis", 120.0, 2
        );

        passenger = new Passenger(
                "P001", "Sarah", "Martin", "sarah@example.com"
        );

        system.addFlight(flight);
    }

    @Test
    void testAddFlight() {
        assertEquals(1, system.getFlights().size());
        assertSame(flight, system.getFlights().get(0));
    }

    @Test
    void testAddNullFlight() {
        assertThrows(IllegalArgumentException.class, () ->
                system.addFlight(null)
        );
    }

    @Test
    void testDuplicateFlight() {
        Flight duplicate = new Flight(
                "BJ101", "Paris", "Tunis", 150.0, 100
        );

        assertThrows(IllegalArgumentException.class, () ->
                system.addFlight(duplicate)
        );
    }

    @Test
    void testDuplicateFlightIgnoreCase() {
        Flight duplicate = new Flight(
                "bj101", "Paris", "Tunis", 150.0, 100
        );

        assertThrows(IllegalArgumentException.class, () ->
                system.addFlight(duplicate)
        );
    }

    @Test
    void testGetFlightsReturnsCopy() {
        List<Flight> copy = system.getFlights();
        copy.clear();

        assertEquals(1, system.getFlights().size());
    }

    @Test
    void testFindFlightByNumber() {
        assertSame(flight,
                system.findFlightByNumber("BJ101"));
    }

    @Test
    void testFindFlightIgnoreCase() {
        assertSame(flight,
                system.findFlightByNumber("bj101"));
    }

    @Test
    void testFindUnknownFlight() {
        assertNull(system.findFlightByNumber("XX999"));
    }

    @Test
    void testSearchFlights() {
        Flight anotherFlight = new Flight(
                "BJ202", "Nice", "Tunis", 150.0, 100
        );

        system.addFlight(anotherFlight);

        List<Flight> results =
                system.searchFlights("Nice", "Tunis");

        assertEquals(2, results.size());
        assertTrue(results.contains(flight));
        assertTrue(results.contains(anotherFlight));
    }

    @Test
    void testSearchFlightsIgnoreCase() {
        List<Flight> results =
                system.searchFlights("nice", "TUNIS");

        assertEquals(1, results.size());
    }

    @Test
    void testSearchFlightsNoResults() {
        assertTrue(
                system.searchFlights("Paris", "London").isEmpty()
        );
    }

    @Test
    void testSearchFlightsInvalidOrigin() {
        assertThrows(IllegalArgumentException.class, () ->
                system.searchFlights(" ", "Tunis")
        );
    }

    @Test
    void testSearchFlightsInvalidDestination() {
        assertThrows(IllegalArgumentException.class, () ->
                system.searchFlights("Nice", null)
        );
    }

    @Test
    void testBookFlight() {
        Booking booking =
                system.bookFlight("B001", passenger, "BJ101");

        assertNotNull(booking);
        assertEquals("B001", booking.getBookingId());
        assertEquals(BookingStatus.CONFIRMED,
                booking.getStatus());
        assertEquals(1, flight.getAvailableSeats());
        assertEquals(1, system.getBookings().size());
    }

    @Test
    void testBookFlightNotFound() {
        assertThrows(IllegalArgumentException.class, () ->
                system.bookFlight("B001", passenger, "XX999")
        );

        assertEquals(2, flight.getAvailableSeats());
        assertTrue(system.getBookings().isEmpty());
    }

    @Test
    void testBookFlightWithNullPassenger() {
        assertThrows(IllegalArgumentException.class, () ->
                system.bookFlight("B001", null, "BJ101")
        );

        assertEquals(2, flight.getAvailableSeats());
    }

    @Test
    void testDuplicateBookingId() {
        system.bookFlight("B001", passenger, "BJ101");

        assertThrows(IllegalArgumentException.class, () ->
                system.bookFlight("B001", passenger, "BJ101")
        );

        assertEquals(1, flight.getAvailableSeats());
        assertEquals(1, system.getBookings().size());
    }

    @Test
    void testBookFlightWhenFull() {
        Passenger p2 = new Passenger(
                "P002", "Adam", "Ali", "adam@example.com"
        );

        Passenger p3 = new Passenger(
                "P003", "Lina", "Ben", "lina@example.com"
        );

        system.bookFlight("B001", passenger, "BJ101");
        system.bookFlight("B002", p2, "BJ101");

        assertThrows(IllegalStateException.class, () ->
                system.bookFlight("B003", p3, "BJ101")
        );

        assertEquals(0, flight.getAvailableSeats());
        assertEquals(2, system.getBookings().size());
    }

    @Test
    void testFindBookingById() {
        Booking booking =
                system.bookFlight("B001", passenger, "BJ101");

        assertSame(booking,
                system.findBookingById("B001"));
    }

    @Test
    void testFindBookingByIdIgnoreCase() {
        Booking booking =
                system.bookFlight("B001", passenger, "BJ101");

        assertSame(booking,
                system.findBookingById("b001"));
    }

    @Test
    void testFindUnknownBooking() {
        assertNull(system.findBookingById("UNKNOWN"));
    }

    @Test
    void testGetBookingsReturnsCopy() {
        system.bookFlight("B001", passenger, "BJ101");

        List<Booking> copy = system.getBookings();
        copy.clear();

        assertEquals(1, system.getBookings().size());
    }

    @Test
    void testCancelBooking() {
        system.bookFlight("B001", passenger, "BJ101");

        assertTrue(system.cancelBooking("B001"));

        assertEquals(2, flight.getAvailableSeats());
        assertEquals(BookingStatus.CANCELLED,
                system.findBookingById("B001").getStatus());
    }

    @Test
    void testCancelBookingTwice() {
        system.bookFlight("B001", passenger, "BJ101");

        assertTrue(system.cancelBooking("B001"));
        assertFalse(system.cancelBooking("B001"));

        assertEquals(2, flight.getAvailableSeats());
    }

    @Test
    void testCancelUnknownBooking() {
        assertFalse(system.cancelBooking("UNKNOWN"));
    }

    @Test
    void testBookAgainAfterCancellation() {
        Passenger p2 = new Passenger(
                "P002", "Adam", "Ali", "adam@example.com"
        );

        system.bookFlight("B001", passenger, "BJ101");
        system.cancelBooking("B001");

        Booking newBooking =
                system.bookFlight("B002", p2, "BJ101");

        assertEquals(BookingStatus.CONFIRMED,
                newBooking.getStatus());

        assertEquals(1, flight.getAvailableSeats());
        assertEquals(2, system.getBookings().size());
    }
}
