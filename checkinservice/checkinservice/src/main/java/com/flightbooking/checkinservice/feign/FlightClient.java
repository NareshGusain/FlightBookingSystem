package com.flightbooking.checkinservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "flightservice")// , url = "http://localhost:8082")
public interface FlightClient {

    @GetMapping("/flights/{flightId}")
    FlightDto getFlightById(@PathVariable Long flightId);
}