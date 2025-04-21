package com.flight.flightservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlightDTO {
    private Long flightId;
    private String flightNumber;
    private String source;
    private String destination;
    private LocalDateTime departureTime;  // Changed from String
    private LocalDateTime arrivalTime;    // Changed from String
    private Integer availableSeats;
    private Double cost;

    // You might want to add formatted date strings for API consumers
    public String getFormattedDepartureTime() {
        return departureTime != null ? departureTime.toString() : null;
    }

    public String getFormattedArrivalTime() {
        return arrivalTime != null ? arrivalTime.toString() : null;
    }
}