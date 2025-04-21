package com.flightbooking.paymentservice.service;

import com.flightbooking.paymentservice.feign.PaymentRequest;
import com.flightbooking.paymentservice.feign.StripeResponse;
import com.flightbooking.paymentservice.model.Payment;
import com.flightbooking.paymentservice.model.PaymentSessionResponse;

import java.util.List;

public interface PaymentService {
    Payment makePayment(Payment payment);

    List<Payment> getAllPayments();
    Payment getPaymentById(Long id);
    StripeResponse makeStripePayment(PaymentRequest paymentrequest);
}