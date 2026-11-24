package com.flightbooking.controller;

import com.flightbooking.Passenger;
import com.flightbooking.dto.ApiError;
import com.flightbooking.repository.PassengerRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import io.swagger.v3.oas.annotations.media.ArraySchema;

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
    @Operation(
            summary = "Retrieve all passengers",
            description = "Returns all passengers stored in the database"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Passengers retrieved successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Passenger.class)
            )
    )
    @GetMapping
    public List<Passenger> getAllPassengers() throws SQLException {
        return passengerRepository.findAll();
    }

    // GET /api/passengers/P003
    @Operation(
            summary = "Find a passenger by ID",
            description = "Returns the details of a specific passenger"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Passengers retrieved successfully",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(
                            schema = @Schema(implementation = Passenger.class)
                    )
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Passenger not found",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiError.class)
            )
    )
    @GetMapping("/{passengerId}")
    public Passenger getPassengerById(
            @Parameter(
                    description = "Unique passenger identifier",
                    example = "P003"
            )
            @PathVariable String passengerId
    ) throws SQLException {

        Passenger passenger = passengerRepository.findById(passengerId);
        if (passenger == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Passenger not found");
        }

        return passenger;
    }
}
