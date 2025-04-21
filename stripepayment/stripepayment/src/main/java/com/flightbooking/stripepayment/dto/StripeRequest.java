package com.flightbooking.stripepayment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StripeRequest {
    private Long bookingId;
    private Double amount;
    private String currency;
    private String description;
}
