package com.flightbooking;

import com.flightbooking.repository.FlightRepository;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.security.test.context.support.WithMockUser;

import com.flightbooking.model.AppUser;
import com.flightbooking.repository.BookingRepository;
import com.flightbooking.service.UserService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.http.MediaType;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FlightRepository flightRepository;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private BookingRepository bookingRepository;



    @Test
    void shouldAllowPublicFlightSearch() throws Exception {

        when(flightRepository.findAll())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/flights"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRequireAuthenticationForBookings()
            throws Exception {

        mockMvc.perform(
                        get("/api/bookings")
                                .param("passengerId", "P003")
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "PASSENGER")
    void shouldDenyBookingsUntilOwnershipChecksExist()
            throws Exception {

        mockMvc.perform(
                        get("/api/bookings")
                                .param("passengerId", "P003")
                )
                .andExpect(status().isForbidden());
    }


    @Test
    void shouldRejectAnonymousMyBookings() throws Exception {
        mockMvc.perform(get("/api/bookings/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(
            username = "lina@example.com",
            roles = "PASSENGER"
    )
    void shouldReturnOnlyAuthenticatedPassengerBookings() throws Exception {

        AppUser lina = new AppUser(
                3L,
                "P003",
                "lina@example.com",
                "dummy-hash",
                true
        );

        when(userService.findByEmail("lina@example.com"))
                .thenReturn(Optional.of(lina));

        when(bookingRepository.findByPassengerId("P003"))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/bookings/me"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(bookingRepository)
                .findByPassengerId("P003");

        verify(bookingRepository, never())
                .findByPassengerId("P001");
    }


    @Test
    @WithMockUser(
            username = "lina@example.com",
            roles = "PASSENGER"
    )
    void shouldRejectBookingForAnotherPassenger()
            throws Exception {

        AppUser lina = new AppUser(
                3L, "P003", "lina@example.com", "test-hash", true
        );

        when(userService.findByEmail("lina@example.com"))
                .thenReturn(Optional.of(lina));

        mockMvc.perform(post("/api/bookings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "bookingId": "B999",
                          "passengerId": "P001",
                          "flightNumber": "BJ101"
                        }
                        """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message")
                        .value("Cannot book for another passenger"));

        verify(bookingRepository, never())
                .bookFlight(anyString(), any(), anyString());
    }

    @Test
    @WithMockUser(username = "lina@example.com", roles = "PASSENGER")
    void shouldNotCancelAnotherPassengerBooking() throws Exception {
        AppUser lina = new AppUser(3L, "P003", "lina@example.com", "test-hash", true);
        when(userService.findByEmail("lina@example.com")).thenReturn(Optional.of(lina));
        when(bookingRepository.cancelBookingForPassenger("B001", "P003")).thenReturn(false);
        mockMvc.perform(patch("/api/bookings/B001/cancel").with(csrf())).andExpect(status().isConflict());
        verify(bookingRepository).cancelBookingForPassenger("B001", "P003");
    }


}
