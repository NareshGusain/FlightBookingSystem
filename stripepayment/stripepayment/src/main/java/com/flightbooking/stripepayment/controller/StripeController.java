package com.flightbooking.stripepayment.controller;

import com.flightbooking.stripepayment.dto.StripeRequest;
import com.flightbooking.stripepayment.dto.StripeResponse;
import com.flightbooking.stripepayment.service.StripeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stripe")
public class StripeController {

    @Autowired
    private StripeService stripeService;

    @PostMapping("/checkout")
    public ResponseEntity<StripeResponse> checkout(@RequestBody StripeRequest request) {
        StripeResponse response = stripeService.createCheckoutSession(request);
        return ResponseEntity.ok(response);
    }
}
