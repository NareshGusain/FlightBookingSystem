package com.flightbooking.paymentservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSessionResponse {
    private String sessionId;
    private String sessionUrl;
    private Long paymentId;
    private PaymentStatus status;
}
