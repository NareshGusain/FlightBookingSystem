package com.flightbooking.paymentservice.feign;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequest {
    private Long bookingId;
//    private Integer amount; // amount in rupees
//    private String email;
}

