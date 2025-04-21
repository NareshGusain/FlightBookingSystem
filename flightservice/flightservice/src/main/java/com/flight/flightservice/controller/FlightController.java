package com.flight.flightservice.controller;

import com.flight.flightservice.dto.FlightDTO;
import com.flight.flightservice.model.Flight;
import com.flight.flightservice.service.FlightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequestMapping("/flights")
public class FlightController {

    @Autowired
    private FlightService flightService;

    public FlightController(FlightService flightService) {
        this.flightService = flightService;
    }

    @PostMapping
    public Flight createFlight(@RequestBody Flight flight) {
        return flightService.saveFlight(flight);
    }

    @GetMapping
    public List<Flight> getAllFlights() {
        return flightService.getAllFlights();
    }

    @GetMapping("/{id}")
    public Flight getFlightById(@PathVariable Long id) {
        return flightService.getFlightById(id);
    }


    @GetMapping("/search")
    public ResponseEntity<List<Flight>> searchFlights(
            @RequestParam String source,
            @RequestParam String destination,
            @RequestParam String date) {
        try {
            // Parse the date string to LocalDate first, then to LocalDateTime
            LocalDate localDate = LocalDate.parse(date);
            LocalDateTime dateTime = localDate.atStartOfDay();

            List<Flight> flights = flightService.searchFlightsByDate(source, destination, dateTime);

            if (flights.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }
            return ResponseEntity.ok(flights);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Please use ISO format (YYYY-MM-DD)", e);
        }
    }


    @PutMapping("/{id}/reduce-seats")
    public ResponseEntity<Flight> reduceAvailableSeats(
            @PathVariable Long id, @RequestParam int passengers) {
        Flight flight = flightService.reduceSeats(id, passengers);
        if (flight == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // If flight not found
        }
        return ResponseEntity.ok(flight);
    }
}
