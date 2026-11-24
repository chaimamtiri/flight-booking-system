package com.flightbooking.controller;

import com.flightbooking.Flight;
import com.flightbooking.repository.FlightRepository;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/api/flights")
public class FlightController {

    private final FlightRepository flightRepository;
    public FlightController(FlightRepository flightRepository) {
        this.flightRepository = flightRepository;
    }

    // GET /api/flights
    @GetMapping
    public List<Flight> getAllFlights() throws SQLException {
        return flightRepository.findAll();
    }

    // GET /api/flights/BJ101
    @GetMapping("/{flightNumber}")
    public Flight getFlightByNumber(@PathVariable String flightNumber) throws SQLException {
        Flight flight = flightRepository.findByNumber(flightNumber);
        if (flight == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Flight not found");
        }
        return flight;
    }
}
