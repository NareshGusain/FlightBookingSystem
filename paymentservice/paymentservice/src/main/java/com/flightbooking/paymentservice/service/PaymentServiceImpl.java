package com.flightbooking.paymentservice.service;

import com.flightbooking.paymentservice.dto.BookingDTO;
import com.flightbooking.paymentservice.dto.UserDTO;
import com.flightbooking.paymentservice.exception.PaymentServiceException;
import com.flightbooking.paymentservice.feign.BookingClient;
import com.flightbooking.paymentservice.feign.PaymentRequest;
import com.flightbooking.paymentservice.feign.StripeResponse;
import com.flightbooking.paymentservice.feign.UserClient;
import com.flightbooking.paymentservice.model.Payment;
import com.flightbooking.paymentservice.model.PaymentStatus;
import com.flightbooking.paymentservice.repository.PaymentRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingClient bookingClient; // Inject Feign Client

    @Autowired
    private UserClient userClient;

    @Value("${stripe.secretKey}")
    private String secretKey;

    @Override
    public Payment makePayment(Payment payment) {
        if (payment.getAmount() <= 0 || payment.getBookingId() == null) {
            throw new IllegalArgumentException("Invalid payment details: amount must be positive and booking ID must exist.");
        }

        payment.setPaymentDate(new Date());
        payment.setTransactionId(UUID.randomUUID().toString());

        // Simulating a success/failure scenario
        boolean isPaymentSuccessful = new Random().nextBoolean();
        payment.setPaymentStatus(isPaymentSuccessful ? PaymentStatus.SUCCESS : PaymentStatus.FAILED);

        Payment savedPayment = paymentRepository.save(payment);

        // If payment is successful, update Booking Status
        if (isPaymentSuccessful) {
            bookingClient.updateBookingStatus(payment.getBookingId(), "CONFIRMED");
        }

        return savedPayment;
    }


    @Override
    public StripeResponse makeStripePayment(PaymentRequest request) {
        Stripe.apiKey = secretKey;

        try {
            // Step 1: Get booking info
            BookingDTO booking = bookingClient.getBookingById(request.getBookingId());
            if (booking == null) {
                throw new PaymentServiceException("Booking not found with ID: " + request.getBookingId());
            }

            // Step 2: Get user info
            UserDTO user = userClient.getUserById(booking.getUserId());
            if (user == null) {
                throw new PaymentServiceException("User not found with ID: " + booking.getUserId());
            }

            // Step 3: Define amount
            long amountInSmallestUnit = Math.round(booking.getTotalAmount()*100);

            // In a real implementation, you might fetch the actual price from a flight service

            // Step 4: Create Stripe session
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl("http://localhost:8084/payments/success?bookingId=" + booking.getBookingId())
                    .setCancelUrl("http://localhost:8084/payments/cancel?bookingId=" + booking.getBookingId())
                    .setCustomerEmail(user.getEmail())
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency("inr")
                                                    .setUnitAmount(amountInSmallestUnit)
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName("Flight Ticket")
                                                                    .setDescription("Booking ID: " + booking.getBookingId() +
                                                                            " | Passenger: " + user.getName() +
                                                                            " | Email: " + user.getEmail())
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    )
                    .build();

            Session session = Session.create(params);

            // Create and save a payment record
            Payment payment = new Payment();
            payment.setBookingId(booking.getBookingId());
            payment.setAmount(booking.getTotalAmount());
            payment.setPaymentDate(new Date());
            payment.setTransactionId(session.getId());
            payment.setPaymentStatus(PaymentStatus.SUCCESS); // Assuming payment is successful when session is created
            paymentRepository.save(payment);

            // Update booking status to CONFIRMED
            bookingClient.updateBookingStatus(booking.getBookingId(), "CONFIRMED");

            return StripeResponse.builder()
                    .status("success")
                    .message("Payment session created successfully for " + user.getName())
                    .sessionId(session.getId())
                    .sessionUrl(session.getUrl())
                    .build();

        } catch (StripeException e) {
            throw new PaymentServiceException("Stripe session creation failed: " + e.getMessage());
        } catch (Exception e) {
            throw new PaymentServiceException("Payment processing failed: " + e.getMessage());
        }
    }


    @Override
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    @Override
    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id).orElse(null);
    }
}