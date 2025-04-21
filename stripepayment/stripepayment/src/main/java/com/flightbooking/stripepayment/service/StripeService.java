package com.flightbooking.stripepayment.service;

import com.flightbooking.stripepayment.dto.StripeRequest;
import com.flightbooking.stripepayment.dto.StripeResponse;

public interface StripeService {
    StripeResponse createCheckoutSession(StripeRequest request);
}
