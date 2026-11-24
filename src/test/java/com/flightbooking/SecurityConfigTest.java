package com.flightbooking;

import com.flightbooking.repository.FlightRepository;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FlightRepository flightRepository;

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
}
