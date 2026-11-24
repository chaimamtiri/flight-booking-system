package com.flightbooking.controller;

import com.flightbooking.Passenger;
import com.flightbooking.repository.PassengerRepository;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/api/passengers")
public class PassengerController {

    private final PassengerRepository passengerRepository;

    public PassengerController(PassengerRepository passengerRepository) {
        this.passengerRepository = passengerRepository;
    }

    // GET /api/passengers
    @GetMapping
    public List<Passenger> getAllPassengers() throws SQLException {
        return passengerRepository.findAll();
    }

    // GET /api/passengers/P001
    @GetMapping("/{passengerId}")
    public Passenger getPassengerById(@PathVariable String passengerId) throws SQLException {
        Passenger passenger = passengerRepository.findById(passengerId);
        if (passenger == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Passenger not found");
        }
        return passenger;
    }
}
