package com.flightbooking.paymentservice.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@AllArgsConstructor
public class BookingDTO {
    private Long bookingId;
    private Long userId;
    private Long flightId;
    private String bookingStatus;
    private Double totalAmount;
}


