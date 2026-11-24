package com.flightbooking;

import com.flightbooking.controller.FlightController;
import com.flightbooking.repository.FlightRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FlightController.class)
class FlightControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FlightRepository flightRepository;

    @Test
    void shouldReturnAllFlights() throws Exception {

        Flight flight1 = new Flight("BJ101", "Nice", "Tunis", 120.0, 180);
        Flight flight2 = new Flight("AF202", "Paris", "Nice", 90.0, 150);
        when(flightRepository.findAll()).thenReturn(List.of(flight1, flight2));
        mockMvc.perform(get("/api/flights"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()")
                        .value(2))
                .andExpect(jsonPath("$[0].flightNumber")
                        .value("BJ101"))
                .andExpect(jsonPath("$[1].flightNumber")
                        .value("AF202"));
    }

    @Test
    void shouldReturnFlightByNumber() throws Exception {
        Flight flight = new Flight("BJ101", "Nice", "Tunis", 120.0, 180);
        when(flightRepository.findByNumber("BJ101")).thenReturn(flight);
        mockMvc.perform(get("/api/flights/BJ101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.origin")
                        .value("Nice"))
                .andExpect(jsonPath("$.destination")
                        .value("Tunis"));
    }

    @Test
    void shouldReturn404ForUnknownFlight() throws Exception {
        when(flightRepository.findByNumber("XX999")).thenReturn(null);
        mockMvc.perform(get("/api/flights/XX999")).andExpect(status().isNotFound());
    }
}
