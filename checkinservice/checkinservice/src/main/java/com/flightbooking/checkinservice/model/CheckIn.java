package com.flightbooking.checkinservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckIn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long checkinId;

    @Lob
    @Column(name = "boarding_pass_pdf", columnDefinition = "LONGBLOB")
    private byte[] boardingPassPdf;


    private Long bookingId;
    private Long flightId;

    @Enumerated(EnumType.STRING)
    private CheckInStatus checkinStatus;

    private LocalTime checkinTime;

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public void setFlightId(Long flightId) {
        this.flightId = flightId;
    }

    public void setCheckinStatus(CheckInStatus checkinStatus) {
        this.checkinStatus = checkinStatus;
    }

    public void setCheckinTime(LocalTime checkinTime) {
        this.checkinTime = checkinTime;
    }


}
