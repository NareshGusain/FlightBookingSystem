package com.flightbooking.paymentservice.controller;

import com.flightbooking.paymentservice.exception.PaymentServiceException;
import com.flightbooking.paymentservice.feign.PaymentRequest;
import com.flightbooking.paymentservice.feign.StripeResponse;
import com.flightbooking.paymentservice.model.Payment;
import com.flightbooking.paymentservice.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/process")
    public Payment processPayment(@RequestParam Long bookingId, @RequestParam Double amount) {
        Payment payment = new Payment();
        payment.setBookingId(bookingId);
        payment.setAmount(amount);
        return paymentService.makePayment(payment);
    }

    @PostMapping("/stripepayment")
    public ResponseEntity<StripeResponse> stripePayment(@RequestBody PaymentRequest paymentRequest) {
        try {
            StripeResponse response = paymentService.makeStripePayment(paymentRequest);
            return ResponseEntity.ok(response);
        } catch (PaymentServiceException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new StripeResponse("error", e.getMessage(), null, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new StripeResponse("error", "An unexpected error occurred", null, null));
        }
    }


    @GetMapping("/{paymentId}")
    public Payment getPayment(@PathVariable Long paymentId) {
        return paymentService.getPaymentById(paymentId);
    }

    @GetMapping
    public List<Payment> getAllPayments() {
        return paymentService.getAllPayments();
    }
}