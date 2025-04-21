package com.flightbooking.paymentservice.feign;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StripeResponse {
    private String status;
    private String message;
    private String sessionId;
    private String sessionUrl;

    public StripeResponse(String id, String url) {
        this.sessionId = id;
        this.sessionUrl = url;

    }
}
