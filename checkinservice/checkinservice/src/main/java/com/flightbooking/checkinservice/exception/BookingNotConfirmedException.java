package com.flightbooking.checkinservice.exception;

public class BookingNotConfirmedException extends RuntimeException {
    public BookingNotConfirmedException(String message) {
        super(message);
    }
}
