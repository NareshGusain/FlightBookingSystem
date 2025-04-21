package com.flight.flightservice.service;

import com.flight.flightservice.model.Flight;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface FlightService {

    // methods declared in interface are implicitly public so not mentioning "public abstract"
    Flight saveFlight(Flight flight);
    List<Flight> getAllFlights();
    Flight getFlightById(Long id);
    Flight reduceSeats(Long id, int passengers);

    List<Flight> searchFlightsByDate(String source, String destination, LocalDateTime date);
}
