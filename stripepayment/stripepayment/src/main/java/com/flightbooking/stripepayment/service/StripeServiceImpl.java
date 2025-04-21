package com.flightbooking.stripepayment.service.impl;

import com.flightbooking.stripepayment.dto.StripeRequest;
import com.flightbooking.stripepayment.dto.StripeResponse;
import com.flightbooking.stripepayment.service.StripeService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.billingportal.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class StripeServiceImpl implements StripeService {

    @Value("${stripe.secretKey}")
    private  String secretKey;
//    @Override
//    public StripeResponse createCheckoutSession(StripeRequest request) {
//        // Create mock session data
//        String sessionId = "dummy_sess_" + UUID.randomUUID();
//        String redirectUrl = "http://localhost:8083/payments/process?bookingId=" +
//                request.getBookingId() + "&amount=" + request.getAmount();
//
//        return StripeResponse.builder()
//                .status("success")
//                .message("Mock Stripe checkout session created")
//                .sessionId(sessionId)
//                .sessionUrl(redirectUrl)
//                .build();
//    }

    public StripeResponse createCheckoutSession(StripeRequest request){
        Stripe.apiKey = secretKey;

        SessionCreateParams.LineItem.PriceData.ProductData productData = SessionCreateParams.LineItem.PriceData.ProductData.builder()
                .setBookingId(request.getBookingId()).build();

        SessionCreateParams.LineItem.PriceData.builder()
                .setCurrency(request.getCurrency() == null?"USD":request.getCurrency())
                .setAmount(request.getAmount())
                .build();

        SessionCreateParams.LineItem lineItem =  SessionCreateParams.LineItem.builder()
                .setBookingId(request.getBookingId())
                .setAmount(request.getAmount())
                .build();


        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSeccessURl()
                .setCancelUrl()
                .addLineItem(lineItem)
                .build();

        Session session = null;
        try {
            session = Session.create(params);
        } catch (StripeException e) {
            //log the error
        }

        return StripeResponse
                .builder()
                .status("SUCCESS")
                .message("Payment session created ")
                .sessionId(session.getBookingId())
                .sessionUrl(session.getUrl())
                .build();
    }
}
