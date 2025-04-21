// BookingRequest.java (in a suitable DTO package)
package com.flight.booking.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookingRequest {
    @NotNull
    private Long userId;

    @NotNull
    private Long flightId;

    @Min(value = 1, message = "Passenger count must be at least 1")
    private int noOfPassengers;
}
