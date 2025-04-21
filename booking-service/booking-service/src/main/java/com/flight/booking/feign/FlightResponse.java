package com.flight.booking.feign;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FlightResponse {
    private Long flightId;
    private String flightNumber;
    private String source;
    private String destination;
    private int availableSeats;
    private Double cost;
}
