package com.flight.booking.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "flightservice")
public interface FlightClient {

    @GetMapping("/flights/{flightId}")
    FlightResponse getFlightById(@PathVariable Long flightId);

    @PutMapping("/flights/{flightId}/reduce-seats")
    void reduceAvailableSeats(@PathVariable Long flightId, @RequestParam int passengers);
}
