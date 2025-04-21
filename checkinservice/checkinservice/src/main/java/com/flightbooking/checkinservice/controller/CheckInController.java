package com.flightbooking.checkinservice.controller;

import com.flightbooking.checkinservice.model.CheckIn;
import com.flightbooking.checkinservice.service.CheckInService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/checkin")
@RequiredArgsConstructor
public class CheckInController {

    private final CheckInService service;

    @PostMapping("/{bookingId}")
    public CheckIn checkIn(@PathVariable Long bookingId) {
        return service.doCheckIn(bookingId);
    }

    @GetMapping()
    public List<CheckIn> getallcheckIn(){
        return service.getAllCheckIns();
    }

    @GetMapping("/{id}")
    public CheckIn getCheckInById(@PathVariable Long id) {
        return service.getCheckInById(id);
    }

    @GetMapping("/pdf/{checkinId}")
    public ResponseEntity<byte[]> getBoardingPassPdf(@PathVariable Long checkinId) {
        byte[] pdfData = service.getBoardingPassPdf(checkinId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.inline().filename("BoardingPass_" + checkinId + ".pdf").build());

        return new ResponseEntity<>(pdfData, headers, HttpStatus.OK);
    }

}