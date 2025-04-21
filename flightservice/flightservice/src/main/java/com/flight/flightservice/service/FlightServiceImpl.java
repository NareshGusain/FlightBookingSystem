package com.flight.flightservice.service;

import com.flight.flightservice.exception.DuplicateFlightException;
import com.flight.flightservice.exception.InvalidInputException;
import com.flight.flightservice.model.Flight;
import com.flight.flightservice.repository.FlightRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class FlightServiceImpl implements FlightService {

    private final FlightRepository flightRepository;

    public FlightServiceImpl(FlightRepository flightRepository) {
        this.flightRepository = flightRepository;
    }

    @Override
    public Flight saveFlight(Flight flight) {
        // Source and Destination must not be the same
        if (flight.getSource() != null && flight.getDestination() != null &&
                flight.getSource().trim().equalsIgnoreCase(flight.getDestination().trim())) {
            throw new InvalidInputException("Flight cannot have same source and destination");
        }

        // Validate capacity and cost
        if (flight.getCapacity() != null && flight.getCapacity() <= 0 ||
                flight.getCost() != null && flight.getCost() < 0) {
            throw new InvalidInputException("Invalid flight details: Capacity and cost must be positive");
        }

        // Validate seat availability
        if (flight.getAvailableSeats() != null && flight.getCapacity() != null &&
                flight.getAvailableSeats() > flight.getCapacity()) {
            throw new InvalidInputException("Available seats cannot be more than total flight capacity");
        }

        // Validate time consistency
        if (flight.getArrivalDateTime() != null && flight.getDepartureDateTime() != null &&
                flight.getArrivalDateTime().isBefore(flight.getDepartureDateTime())) {
            throw new InvalidInputException("Arrival time must be after departure time");
        }

        // Prevent duplicate flight number on the same day
        if (flight.getFlightNumber() != null && flight.getDepartureDateTime() != null) {
            LocalDate departureDate = flight.getDepartureDateTime().toLocalDate();
            LocalDateTime startOfDay = departureDate.atStartOfDay();
            LocalDateTime endOfDay = departureDate.plusDays(1).atStartOfDay().minusNanos(1);

            if (flightRepository.existsByFlightNumberAndDepartureDateTimeBetween(
                    flight.getFlightNumber(), startOfDay, endOfDay)) {
                throw new DuplicateFlightException("Flight with number " + flight.getFlightNumber() +
                        " already exists on " + departureDate);
            }
        }
        return flightRepository.save(flight);
    }





    @Override
    public List<Flight> getAllFlights() {
        return flightRepository.findAll();
    }

    @Override
    public Flight getFlightById(Long id) {
        return flightRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Flight not found with ID: " + id)
        );
    }


    @Override
    public Flight reduceSeats(Long id, int passengers) {
        Optional<Flight> optionalFlight = flightRepository.findById(id);
        if (optionalFlight.isPresent()) {
            Flight flight = optionalFlight.get();
            int availableSeats = flight.getAvailableSeats();

            // Reduce the seats only if there are enough available
            if (availableSeats >= passengers) {
                flight.setAvailableSeats(availableSeats - passengers);
                flightRepository.save(flight); // Save the updated flight
                return flight;
            }
        }
        return null; // Return null if flight not found or insufficient seats
    }

    @Override
    public List<Flight> searchFlightsByDate(String source, String destination, LocalDateTime date) {
        if (source == null || destination == null || date == null) {
            throw new IllegalArgumentException("Source, destination, and date must not be null");
        }

        // Validate input parameters
        if (source.isBlank() || destination.isBlank()) {
            throw new IllegalArgumentException("Source and destination must not be empty");
        }

        // Create start and end of the selected date
        LocalDateTime startOfDay = date.withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = date.withHour(23).withMinute(59).withSecond(59).withNano(999999999);

        // Return flights, even if the list is empty
        return flightRepository.findBySourceAndDestinationAndDepartureDateTimeBetween(
                source, destination, startOfDay, endOfDay);
    }
}
