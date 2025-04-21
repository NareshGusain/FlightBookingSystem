package com.flight.booking.controller;

import com.flight.booking.dto.BookingRequest;
import com.flight.booking.model.Booking;
import com.flight.booking.model.BookingStatus;
import com.flight.booking.repository.BookingRepository;
import com.flight.booking.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bookings")
public class BookingController {

//    Once the bookingService object is assigned (via constructor), it cannot be reassigned.
//    This ensures that the BookingController will always use the same BookingService instance throughout its lifecycle.
    private final BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public Booking createBooking(@RequestBody @Valid BookingRequest bookingRequest) {
        return bookingService.createBooking(bookingRequest);
    }

    @GetMapping
    public List<Booking> getAllBookings() {
        return bookingService.getAllBookings();
    }

    @GetMapping("/{id}")
    public Booking getBookingById(@PathVariable Long id) {
        return bookingService.getBookingById(id);
    }

    @DeleteMapping("/{id}")
    public String deleteBooking(@PathVariable Long id) {
        bookingService.deleteBooking(id);
        return "Booking deleted successfully!";
    }


    @PutMapping("/{bookingId}/update-status")
    public String updateBookingStatus(@PathVariable Long bookingId, @RequestParam String status) {
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking != null) {
            booking.setBookingStatus(BookingStatus.valueOf(status));
            bookingRepository.save(booking);
            return "Booking status updated to: " + status;
        }
        return "Booking not found";
    }


    // the below method is for checkin service
    @GetMapping("/status/{bookingId}")
    public String getBookingStatus(@PathVariable Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        return booking.getBookingStatus().name(); // assuming Enum
    }

    @GetMapping("/flightId/{bookingId}")
    public Long getFlightIdFromBooking(@PathVariable Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found to fetch flight Id"));
        return booking.getFlightId(); // assuming this method or field exists
    }

    @GetMapping("/userId/{bookingId}")
    public Long getUserIdFromBooking(@PathVariable Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found to fetch User Id"));
        return booking.getUserId();
    }

}
