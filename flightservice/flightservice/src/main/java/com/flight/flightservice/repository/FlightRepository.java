package com.flight.flightservice.repository;

import com.flight.flightservice.model.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FlightRepository extends JpaRepository<Flight, Long> {
    List<Flight> findBySourceAndDestinationAndDepartureDateTimeBetween(String source, String destination, LocalDateTime startDateTime, LocalDateTime endDateTime);

    boolean existsByFlightNumberAndDepartureDateTimeBetween(String flightNumber, LocalDateTime startOfDay, LocalDateTime endOfDay);
}
