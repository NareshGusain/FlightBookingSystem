package com.flightbooking.paymentservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    private Long bookingId;  // Foreign key (Reference to Booking)
    private Double amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus; // PENDING, SUCCESS, FAILED

    private Date paymentDate;
    private String transactionId;

    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
    public void setAmount(Double amount) { this.amount = amount; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }
    public void setPaymentDate(Date paymentDate) { this.paymentDate = paymentDate; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }


    public double getAmount() {
        return amount;
    }

    public Long getBookingId() {
        return bookingId;
    }
}