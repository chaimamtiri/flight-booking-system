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

import com.flightbooking.dto.BookingSummary;
import java.util.List;

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


    @Test
    void shouldReturnPassengerBookings() throws Exception {
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

        when(bookingRepository.findByPassengerId("P003")).thenReturn(List.of(booking1, booking2));
        mockMvc.perform(get("/api/bookings").param("passengerId", "P003"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].bookingId").value("B003"))
                .andExpect(jsonPath("$[0].status").value("CANCELLED"))
                .andExpect(jsonPath("$[1].bookingId").value("B004"))
                .andExpect(jsonPath("$[1].status").value("CONFIRMED"));
    }

    @Test
    void shouldReturnEmptyListWhenNoBookings() throws Exception {
        when(bookingRepository.findByPassengerId("P002")).thenReturn(List.of());
        mockMvc.perform(get("/api/bookings").param("passengerId", "P002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldRejectEmptyPassengerId() throws Exception {
        mockMvc.perform(get("/api/bookings").param("passengerId", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Passenger ID is required"));
    }


}
