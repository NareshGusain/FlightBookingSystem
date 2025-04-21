package com.flightbooking.checkinservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "booking-service")
public interface BookingClient {

    @GetMapping("/bookings/status/{bookingId}")
    String getBookingStatus(@PathVariable Long bookingId);

    @GetMapping("/bookings/flightId/{bookingId}")
    Long getFlightIdFromBooking(@PathVariable Long bookingId);

    @GetMapping("/bookings/userId/{bookingId}")
    Long getUserIdFromBooking(@PathVariable Long bookingId);

    @GetMapping("/bookings/seatNumber/{bookingId}")
    String getSeatNumberForBooking(@PathVariable Long bookingId);
}