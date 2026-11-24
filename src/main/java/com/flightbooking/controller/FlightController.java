package com.flightbooking.controller;

// Classes de mon projet
import com.flightbooking.Flight;
import com.flightbooking.dto.ApiError;
import com.flightbooking.repository.FlightRepository;

// Swagger
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

// Spring Boot
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

// Java
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
    @Operation(
            summary = "Retrieve all flights",
            description = "Returns all flights stored in the database"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Flights retrieved successfully"
    )
    @GetMapping
    public List<Flight> getAllFlights() throws SQLException {
        return flightRepository.findAll();
    }

    // GET /api/flights/BJ101
    @Operation(
            summary = "Find a flight by its number",
            description = "Returns the details of a specific flight"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Flight found successfully"
    )
    @ApiResponse(
            responseCode = "404",
            description = "Flight not found",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @GetMapping("/{flightNumber}")
    public Flight getFlightByNumber(
            @Parameter(
                    description = "Unique flight number",
                    example = "BJ101"
            )
            @PathVariable String flightNumber
    ) throws SQLException {
        Flight flight = flightRepository.findByNumber(flightNumber);
        if (flight == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Flight not found");
        }
        return flight;
    }
}
