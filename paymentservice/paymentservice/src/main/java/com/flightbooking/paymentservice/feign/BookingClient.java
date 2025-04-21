package com.flightbooking.paymentservice.feign;

import com.flightbooking.paymentservice.dto.BookingDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "booking-service") // Must match service name in Eureka
public interface BookingClient {

    @GetMapping("/bookings/{id}")
    BookingDTO getBookingById(@PathVariable("id") Long id);

    @PutMapping("/bookings/{bookingId}/update-status")
    void updateBookingStatus(@PathVariable Long bookingId, @RequestParam String status);
}
