
package com.flightbooking;

import com.flightbooking.controller.BookingController;
import com.flightbooking.repository.BookingRepository;
import com.flightbooking.repository.PassengerRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookingRepository bookingRepository;

    @MockitoBean
    private PassengerRepository passengerRepository;

    @Test
    void shouldCreateBooking() throws Exception {
        Passenger passenger = new Passenger("P003", "Lina", "Ben Ali", "lina@example.com");
        Flight flight = new Flight("BJ101", "Nice", "Tunis", 120.0, 180);
        Booking booking = new Booking("B003", passenger, flight);
        when(passengerRepository.findById("P003")).thenReturn(passenger);
        when(bookingRepository.bookFlight(eq("B003"), eq(passenger), eq("BJ101"))).thenReturn(booking);
        mockMvc.perform(post("/api/bookings").contentType(MediaType.APPLICATION_JSON).content("""
                    {
                      "bookingId": "B003",
                      "passengerId": "P003",
                      "flightNumber": "BJ101"
                    }
                    """))
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

    @Test
    void shouldRejectMissingBookingInformation() throws Exception {
        mockMvc.perform(post("/api/bookings").contentType(MediaType.APPLICATION_JSON).content("""
                    {
                      "bookingId": "",
                      "passengerId": "P003",
                      "flightNumber": "BJ101"
                    }
                    """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldCancelBooking() throws Exception {
        when(bookingRepository.cancelBooking("B003")).thenReturn(true);
        mockMvc.perform(patch("/api/bookings/B003/cancel")).andExpect(status().isNoContent());
    }

    @Test
    void shouldRejectAlreadyCancelledBooking() throws Exception {
        when(bookingRepository.cancelBooking("B003")).thenReturn(false);
        mockMvc.perform(patch("/api/bookings/B003/cancel")).andExpect(status().isConflict());
    }


    @Test
    void shouldRejectMissingPassengerId() throws Exception {
        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                  "bookingId": "B100",
                  "passengerId": "",
                  "flightNumber": "BJ101"
                }
                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("passengerId: Passenger ID is required"))
                .andExpect(jsonPath("$.path").value("/api/bookings"));
    }

    @Test
    void shouldReturnJsonForDoubleCancellation() throws Exception {
        when(bookingRepository.cancelBooking("B003")).thenReturn(false);
        mockMvc.perform(patch("/api/bookings/B003/cancel")).andExpect(status().isConflict()).andExpect(jsonPath("$.status").value(409)).andExpect(jsonPath("$.message").value("Booking not found or already cancelled"));
    }

}
