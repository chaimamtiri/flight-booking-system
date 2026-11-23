
package com.flightbooking;

import com.flightbooking.database.DatabaseConnection;
import com.flightbooking.repository.BookingRepository;
import com.flightbooking.repository.FlightRepository;
import com.flightbooking.repository.PassengerRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class BookingRepositoryIntegrationTest {

    private BookingRepository bookingRepository;
    private FlightRepository flightRepository;
    private PassengerRepository passengerRepository;

    private String flightId;
    private String passengerId;
    private String bookingId;
    private Passenger passenger;

    @BeforeAll
    static void prepareDatabase() throws SQLException {

        String url = System.getenv("DB_URL");

        // Sécurité : ne jamais lancer ces tests sur
        // la base principale.
        assumeTrue(
                "jdbc:postgresql://localhost:5432/flight_booking_test_db"
                        .equals(url),
                "Integration tests require the dedicated test database"
        );

        try (Connection connection =
                     DatabaseConnection.getConnection();
             Statement statement = connection.createStatement()) {

            statement.execute("""
                CREATE TABLE IF NOT EXISTS flights (
                    flight_number VARCHAR(20) PRIMARY KEY,
                    origin VARCHAR(100) NOT NULL,
                    destination VARCHAR(100) NOT NULL,
                    price NUMERIC(10,2) NOT NULL CHECK (price >= 0),
                    total_seats INTEGER NOT NULL CHECK (total_seats >= 0),
                    available_seats INTEGER NOT NULL,
                    CHECK (available_seats BETWEEN 0 AND total_seats)
                )
                """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS passengers (
                    passenger_id VARCHAR(20) PRIMARY KEY,
                    first_name VARCHAR(100) NOT NULL,
                    last_name VARCHAR(100) NOT NULL,
                    email VARCHAR(255) NOT NULL
                )
                """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS bookings (
                    booking_id VARCHAR(20) PRIMARY KEY,
                    passenger_id VARCHAR(20) NOT NULL
                        REFERENCES passengers(passenger_id),
                    flight_number VARCHAR(20) NOT NULL
                        REFERENCES flights(flight_number),
                    status VARCHAR(20) NOT NULL
                        CHECK (status IN ('CONFIRMED', 'CANCELLED'))
                )
                """);
        }
    }

    @BeforeEach
    void setUp() throws SQLException {

        bookingRepository = new BookingRepository();
        flightRepository = new FlightRepository();
        passengerRepository = new PassengerRepository();

        String id = UUID.randomUUID().toString()
                .replace("-", "").substring(0, 12);

        flightId = "F" + id;
        passengerId = "P" + id;
        bookingId = "B" + id;

        Flight flight = new Flight(
                flightId, "Nice", "Tunis", 120.0, 2
        );

        passenger = new Passenger(
                passengerId, "Sarah", "Martin",
                "sarah@example.com"
        );

        flightRepository.save(flight);
        passengerRepository.save(passenger);
    }

    @Test
    void testSuccessfulBooking() throws SQLException {

        Booking booking = bookingRepository.bookFlight(
                bookingId, passenger, flightId
        );

        assertEquals(BookingStatus.CONFIRMED,
                booking.getStatus());

        assertEquals(1, flightRepository
                .findByNumber(flightId).getAvailableSeats());
    }

    @Test
    void testDuplicateBookingRollback() throws SQLException {

        bookingRepository.bookFlight(
                bookingId, passenger, flightId
        );

        assertThrows(SQLException.class, () ->
                bookingRepository.bookFlight(
                        bookingId, passenger, flightId
                )
        );

        // Une seule place doit avoir été réservée.
        assertEquals(1, flightRepository
                .findByNumber(flightId).getAvailableSeats());
    }

    @Test
    void testCancellationReleasesSeat() throws SQLException {

        bookingRepository.bookFlight(
                bookingId, passenger, flightId
        );

        assertTrue(bookingRepository.cancelBooking(bookingId));

        assertEquals(2, flightRepository
                .findByNumber(flightId).getAvailableSeats());
    }

    @Test
    void testCannotCancelTwice() throws SQLException {

        bookingRepository.bookFlight(
                bookingId, passenger, flightId
        );

        assertTrue(bookingRepository.cancelBooking(bookingId));

        assertFalse(bookingRepository.cancelBooking(bookingId));

        assertEquals(2, flightRepository
                .findByNumber(flightId).getAvailableSeats());
    }

    @Test
    void testBookingWhenFlightFull() throws SQLException {

        bookingRepository.bookFlight(
                bookingId, passenger, flightId
        );

        bookingRepository.bookFlight(
                bookingId + "X", passenger, flightId
        );

        assertThrows(IllegalStateException.class, () ->
                bookingRepository.bookFlight(
                        bookingId + "Y", passenger, flightId
                )
        );

        assertEquals(0, flightRepository
                .findByNumber(flightId).getAvailableSeats());
    }

    @AfterEach
    void cleanUp() throws SQLException {

        try (Connection connection =
                     DatabaseConnection.getConnection()) {

            // Supprimer les réservations de ce vol
            try (PreparedStatement statement =
                         connection.prepareStatement(
                                 "DELETE FROM bookings WHERE flight_number = ?"
                         )) {

                statement.setString(1, flightId);
                statement.executeUpdate();
            }

            // Supprimer le passager de test
            try (PreparedStatement statement =
                         connection.prepareStatement(
                                 "DELETE FROM passengers WHERE passenger_id = ?"
                         )) {

                statement.setString(1, passengerId);
                statement.executeUpdate();
            }

            // Supprimer le vol de test
            try (PreparedStatement statement =
                         connection.prepareStatement(
                                 "DELETE FROM flights WHERE flight_number = ?"
                         )) {

                statement.setString(1, flightId);
                statement.executeUpdate();
            }
        }
    }
}
