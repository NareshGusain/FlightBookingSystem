package com.flight.booking.service;

import com.flight.booking.dto.BookingRequest;
import com.flight.booking.exception.BookingException;
import com.flight.booking.feign.FlightClient;
import com.flight.booking.feign.FlightResponse;
import com.flight.booking.feign.UserClient;
import com.flight.booking.feign.UserResponse;
import com.flight.booking.model.Booking;
import com.flight.booking.model.BookingStatus;
import com.flight.booking.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final FlightClient flightClient;  // Inject FlightClient
    private final UserClient userClient;

    @Autowired // we tell spring to inject these dependencies, when creating the BookingService bean.
    public BookingService(BookingRepository bookingRepository, FlightClient flightClient, UserClient userClient) {
        this.bookingRepository = bookingRepository;
        this.flightClient = flightClient;
        this.userClient = userClient;
    }



    public Booking createBooking(BookingRequest bookingRequest) {
        // Validate user
        UserResponse user = userClient.getUserById(bookingRequest.getUserId());
        if (user == null) throw new BookingException("User not found!");

        // Validate flight
        FlightResponse flight = flightClient.getFlightById(bookingRequest.getFlightId());
        if (flight == null) throw new BookingException ("Flight not found!");

        int requestedSeats = bookingRequest.getNoOfPassengers();
        if (flight.getAvailableSeats() < requestedSeats)
            throw new BookingException ("Not enough seats available!");

        // Reduce seats
        flightClient.reduceAvailableSeats(bookingRequest.getFlightId(), requestedSeats);

        double totalAmount = flight.getCost() * requestedSeats;
        Booking booking = Booking.builder()
                .userId(bookingRequest.getUserId())
                .flightId(bookingRequest.getFlightId())
                .noOfPassengers(requestedSeats)
                .totalAmount(totalAmount)
                .bookingStatus(BookingStatus.PENDING)
                .build();

        return bookingRepository.save(booking);
    }


    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id).orElse(null);
    }

    public void deleteBooking(Long id) {
        bookingRepository.deleteById(id);
    }


}
