
package com.flightbooking;

import com.flightbooking.controller.BookingController;
import com.flightbooking.dto.BookingSummary;
import com.flightbooking.model.AppUser;
import com.flightbooking.repository.BookingRepository;
import com.flightbooking.repository.PassengerRepository;
import com.flightbooking.service.UserService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
@AutoConfigureMockMvc(addFilters = false)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookingRepository bookingRepository;

    @MockitoBean
    private PassengerRepository passengerRepository;

    @MockitoBean
    private UserService userService;

    // --------------------------------------------------
    // Helpers
    // --------------------------------------------------

    private void mockLinaAccount() throws Exception {

        AppUser lina = new AppUser(
                3L,
                "P003",
                "lina@example.com",
                "test-hash",
                true
        );

        when(userService.findByEmail("lina@example.com"))
                .thenReturn(Optional.of(lina));
    }

    private TestingAuthenticationToken authenticatedLina() {

        return new TestingAuthenticationToken(
                "lina@example.com",
                "test-password",
                "ROLE_PASSENGER"
        );
    }

    // --------------------------------------------------
    // Test 1: Create a booking
    // --------------------------------------------------

    @Test
    void shouldCreateBooking() throws Exception {

        mockLinaAccount();

        Passenger passenger = new Passenger(
                "P003",
                "Lina",
                "Ben Ali",
                "lina@example.com"
        );

        Flight flight = new Flight(
                "BJ101",
                "Nice",
                "Tunis",
                120.0,
                180
        );

        Booking booking = new Booking(
                "B003",
                passenger,
                flight
        );

        when(passengerRepository.findById("P003"))
                .thenReturn(passenger);

        when(bookingRepository.bookFlight(
                eq("B003"),
                eq(passenger),
                eq("BJ101")
        )).thenReturn(booking);

        mockMvc.perform(
                        post("/api/bookings")
                                .principal(authenticatedLina())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {
                                  "bookingId": "B003",
                                  "passengerId": "P003",
                                  "flightNumber": "BJ101"
                                }
                                """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookingId")
                        .value("B003"))
                .andExpect(jsonPath("$.passengerId")
                        .value("P003"))
                .andExpect(jsonPath("$.flightNumber")
                        .value("BJ101"))
                .andExpect(jsonPath("$.status")
                        .value("CONFIRMED"));
    }

    // --------------------------------------------------
    // Test 2: Reject missing booking ID
    // --------------------------------------------------

    @Test
    void shouldRejectMissingBookingInformation()
            throws Exception {

        mockMvc.perform(
                        post("/api/bookings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {
                                  "bookingId": "",
                                  "passengerId": "P003",
                                  "flightNumber": "BJ101"
                                }
                                """)
                )
                .andExpect(status().isBadRequest());
    }

    // --------------------------------------------------
    // Test 3: Cancel an existing booking
    // --------------------------------------------------

    @Test
    void shouldCancelBooking() throws Exception {

        mockLinaAccount();

        when(bookingRepository.cancelBookingForPassenger(
                "B003",
                "P003"
        )).thenReturn(true);

        mockMvc.perform(
                        patch("/api/bookings/B003/cancel")
                                .principal(authenticatedLina())
                )
                .andExpect(status().isNoContent());
    }

    // --------------------------------------------------
    // Test 4: Reject an already cancelled booking
    // --------------------------------------------------

    @Test
    void shouldRejectAlreadyCancelledBooking()
            throws Exception {

        mockLinaAccount();

        when(bookingRepository.cancelBookingForPassenger(
                "B003",
                "P003"
        )).thenReturn(false);

        mockMvc.perform(
                        patch("/api/bookings/B003/cancel")
                                .principal(authenticatedLina())
                )
                .andExpect(status().isConflict());
    }

    // --------------------------------------------------
    // Test 5: Reject missing passenger ID
    // --------------------------------------------------

    @Test
    void shouldRejectMissingPassengerId()
            throws Exception {

        mockMvc.perform(
                        post("/api/bookings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {
                                  "bookingId": "B100",
                                  "passengerId": "",
                                  "flightNumber": "BJ101"
                                }
                                """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status")
                        .value(400))
                .andExpect(jsonPath("$.message")
                        .value(
                                "passengerId: Passenger ID is required"
                        ))
                .andExpect(jsonPath("$.path")
                        .value("/api/bookings"));
    }

    // --------------------------------------------------
    // Test 6: Return JSON for double cancellation
    // --------------------------------------------------

    @Test
    void shouldReturnJsonForDoubleCancellation()
            throws Exception {

        mockLinaAccount();

        when(bookingRepository.cancelBookingForPassenger(
                "B003",
                "P003"
        )).thenReturn(false);

        mockMvc.perform(
                        patch("/api/bookings/B003/cancel")
                                .principal(authenticatedLina())
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status")
                        .value(409))
                .andExpect(jsonPath("$.message")
                        .value(
                                "Booking unavailable or already cancelled"
                        ));
    }

    // --------------------------------------------------
    // Test 7: Return passenger bookings
    // --------------------------------------------------

    @Test
    void shouldReturnPassengerBookings()
            throws Exception {

        BookingSummary booking1 = new BookingSummary(
                "B003",
                "P003",
                "BJ101",
                "CANCELLED"
        );

        BookingSummary booking2 = new BookingSummary(
                "B004",
                "P003",
                "BJ101",
                "CONFIRMED"
        );

        when(bookingRepository.findByPassengerId("P003"))
                .thenReturn(List.of(booking1, booking2));

        mockMvc.perform(
                        get("/api/bookings")
                                .param("passengerId", "P003")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()")
                        .value(2))
                .andExpect(jsonPath("$[0].bookingId")
                        .value("B003"))
                .andExpect(jsonPath("$[0].status")
                        .value("CANCELLED"))
                .andExpect(jsonPath("$[1].bookingId")
                        .value("B004"))
                .andExpect(jsonPath("$[1].status")
                        .value("CONFIRMED"));
    }

    // --------------------------------------------------
    // Test 8: Return an empty list
    // --------------------------------------------------

    @Test
    void shouldReturnEmptyListWhenNoBookings()
            throws Exception {

        when(bookingRepository.findByPassengerId("P002"))
                .thenReturn(List.of());

        mockMvc.perform(
                        get("/api/bookings")
                                .param("passengerId", "P002")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()")
                        .value(0));
    }

    // --------------------------------------------------
    // Test 9: Reject empty passenger ID
    // --------------------------------------------------

    @Test
    void shouldRejectEmptyPassengerId()
            throws Exception {

        mockMvc.perform(
                        get("/api/bookings")
                                .param("passengerId", "")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Passenger ID is required"));
    }
}
