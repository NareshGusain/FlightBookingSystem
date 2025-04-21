package com.flightbooking.checkinservice.service;

import com.flightbooking.checkinservice.exception.BookingNotConfirmedException;
import com.flightbooking.checkinservice.feign.BookingClient;
import com.flightbooking.checkinservice.feign.FlightClient;
import com.flightbooking.checkinservice.feign.FlightDto;
import com.flightbooking.checkinservice.feign.UserClient;
import com.flightbooking.checkinservice.feign.UserDto;
import com.flightbooking.checkinservice.model.CheckIn;
import com.flightbooking.checkinservice.model.CheckInStatus;
import com.flightbooking.checkinservice.repository.CheckInRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckInService {

    private final CheckInRepository repository;
    private final BookingClient bookingClient;
    private final UserClient userClient;
    private final FlightClient flightClient;
    private final EmailService emailService;

    public CheckIn doCheckIn(Long bookingId) {
        String bookingStatus = bookingClient.getBookingStatus(bookingId);

        if (!"CONFIRMED".equalsIgnoreCase(bookingStatus)) {
            throw new BookingNotConfirmedException("Booking is not confirmed, cannot check in.");
        }

        Long flightId = bookingClient.getFlightIdFromBooking(bookingId);

        // Default values to use if service calls fail
        Long userId = null;
        String seatNumber = generateRandomSeatNumber();
        UserDto user = null;
        FlightDto flight = null;

        // Try to get userId, but don't fail if endpoint doesn't exist
        try {
            userId = bookingClient.getUserIdFromBooking(bookingId);
            // Try to get user info if userId is available
            if (userId != null) {
                try {
                    user = userClient.getUserById(userId);
                } catch (FeignException e) {
                    log.warn("Failed to get user details: {}", e.getMessage());
                }
            }
        } catch (FeignException e) {
            log.warn("Failed to get userId from booking: {}", e.getMessage());
        }


        // Try to get flight details
        try {
            flight = flightClient.getFlightById(flightId);
        } catch (FeignException e) {
            log.warn("Failed to get flight details: {}", e.getMessage());
        }

        CheckIn checkIn = new CheckIn();
        checkIn.setBookingId(bookingId);
        checkIn.setFlightId(flightId);
        checkIn.setCheckinStatus(CheckInStatus.CHECKED_IN);
        checkIn.setCheckinTime(LocalTime.now());

        CheckIn saved = repository.save(checkIn); // Save first to generate checkInId

        byte[] pdfBytes = generateBoardingPassPdfAsBytes(saved, user, flight, seatNumber);
        saved.setBoardingPassPdf(pdfBytes);

        CheckIn finalCheckIn = repository.save(saved); // Save again with PDF added

        // Send email with boarding pass if user email is available
        if (user != null && user.getEmail() != null) {
            sendBoardingPassEmail(user.getEmail(), finalCheckIn, flight);
        } else {
            log.warn("Unable to send boarding pass email: user email not available");
        }

        return finalCheckIn;
    }

    public List<CheckIn> getAllCheckIns() {
        return repository.findAll();
    }

    public CheckIn getCheckInById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Check-in not found with ID: " + id));
    }

    public byte[] getBoardingPassPdf(Long checkinId) {
        CheckIn checkIn = repository.findById(checkinId)
                .orElseThrow(() -> new RuntimeException("Check-in not found with ID: " + checkinId));

        if (checkIn.getBoardingPassPdf() == null) {
            throw new RuntimeException("Boarding pass not generated for this check-in.");
        }

        return checkIn.getBoardingPassPdf();
    }

    private byte[] generateBoardingPassPdfAsBytes(CheckIn checkIn, UserDto user, FlightDto flight, String seatNumber) {
        try {
            Document document = new Document(new Rectangle(500, 300)); // Boarding pass size
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfWriter writer = PdfWriter.getInstance(document, outputStream);
            document.open();

            // Fonts
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.DARK_GRAY);
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.DARK_GRAY);
            Font normalFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.BLACK);
            Font largeFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, BaseColor.DARK_GRAY);

            // Main table with 3 columns
            PdfPTable mainTable = new PdfPTable(3);
            mainTable.setWidthPercentage(100);
            mainTable.setWidths(new float[]{2f, 6f, 2f});

            // Logo Cell
            PdfPCell logoCell = new PdfPCell();
            Paragraph airlineName = new Paragraph("SWIFT", largeFont);
            airlineName.setAlignment(Element.ALIGN_CENTER);
            logoCell.addElement(airlineName);
            Paragraph airlineSubtext = new Paragraph("AIRLINES", titleFont);
            airlineSubtext.setAlignment(Element.ALIGN_CENTER);
            logoCell.addElement(airlineSubtext);
            logoCell.setBorder(Rectangle.NO_BORDER);
            logoCell.setPadding(10);
            mainTable.addCell(logoCell);

            // Center information cell
            PdfPCell infoCell = new PdfPCell();
            infoCell.setBorder(Rectangle.NO_BORDER);

            // Passenger info
            Paragraph passengerInfo = new Paragraph("BOARDING PASS", headerFont);
            passengerInfo.setAlignment(Element.ALIGN_CENTER);
            infoCell.addElement(passengerInfo);

            PdfPTable passengerTable = new PdfPTable(2);
            passengerTable.setWidthPercentage(100);

            // Row 1 - Passenger Name
            passengerTable.addCell(createLabelCell("PASSENGER NAME", titleFont));
            String passengerName = user != null ? user.getName() : "Passenger";
            passengerTable.addCell(createValueCell(passengerName, normalFont));

            // Row 2 & 3 - Flight Source and Destination
            String source = flight != null ? flight.getSource() : "Source";
            String destination = flight != null ? flight.getDestination() : "Destination";

            passengerTable.addCell(createLabelCell("FROM", titleFont));
            passengerTable.addCell(createValueCell(source, normalFont));

            passengerTable.addCell(createLabelCell("TO", titleFont));
            passengerTable.addCell(createValueCell(destination, normalFont));

            // Row 4 - Flight Number
            passengerTable.addCell(createLabelCell("FLIGHT", titleFont));
            String flightNumber = flight != null ? flight.getFlightNumber() : "FL" + checkIn.getFlightId();
            passengerTable.addCell(createValueCell(flightNumber, normalFont));

            // Row 5 & 6 - Date and Time
            String departureDate;
            String departureTime;
            LocalDateTime now = LocalDateTime.now();

            if (flight != null && flight.getDepartureDateTime() != null) {
                departureDate = flight.getDepartureDateTime().toLocalDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
                departureTime = flight.getDepartureDateTime().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"));
            } else {
                departureDate = now.toLocalDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
                departureTime = now.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"));
            }

            passengerTable.addCell(createLabelCell("DATE", titleFont));
            passengerTable.addCell(createValueCell(departureDate, normalFont));

            passengerTable.addCell(createLabelCell("TIME", titleFont));
            passengerTable.addCell(createValueCell(departureTime, normalFont));

            // Row 7 - Seat
            passengerTable.addCell(createLabelCell("SEAT", titleFont));
            passengerTable.addCell(createValueCell(seatNumber, normalFont));

            // Row 8 - Boarding Time
            passengerTable.addCell(createLabelCell("BOARDING TIME", titleFont));
            // Calculate boarding time as 45 minutes before departure
            LocalDateTime departureDateTime = flight != null && flight.getDepartureDateTime() != null ?
                    flight.getDepartureDateTime() : now.plusHours(2);
            String boardingTime = departureDateTime.minusMinutes(45).toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"));
            passengerTable.addCell(createValueCell(boardingTime, normalFont));

            // Add passenger table to info cell
            infoCell.addElement(passengerTable);
            mainTable.addCell(infoCell);

            // QR Code Cell
            PdfPCell qrCell = new PdfPCell();
            qrCell.setBorder(Rectangle.NO_BORDER);
            qrCell.setPadding(10);

            // Generate QR code
            String qrData = "CheckInID:" + checkIn.getCheckinId() +
                    " | Name:" + passengerName +
                    " | Flight:" + flightNumber +
                    " | Seat:" + seatNumber +
                    " | From:" + source +
                    " | To:" + destination +
                    " | Date:" + departureDate +
                    " | Time:" + departureTime;

            Image qrImage = generateQRCodeImage(qrData);
            qrImage.setAlignment(Element.ALIGN_CENTER);
            qrCell.addElement(qrImage);

            // Add gate info
            Paragraph gateInfo = new Paragraph("GATE", titleFont);
            gateInfo.setAlignment(Element.ALIGN_CENTER);
            qrCell.addElement(gateInfo);

            // Random gate number
            Paragraph gateNumber = new Paragraph(generateRandomGate(), largeFont);
            gateNumber.setAlignment(Element.ALIGN_CENTER);
            qrCell.addElement(gateNumber);

            mainTable.addCell(qrCell);

            // Add the main table to document
            document.add(mainTable);

            // Add separator line
            PdfPTable separatorTable = new PdfPTable(1);
            separatorTable.setWidthPercentage(100);
            PdfPCell separatorCell = new PdfPCell(new Phrase("✂ - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -"));
            separatorCell.setBorder(Rectangle.NO_BORDER);
            separatorCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            separatorTable.addCell(separatorCell);
            document.add(separatorTable);

            // Add footer with additional information
            Paragraph footer = new Paragraph("Please arrive at the airport at least 2 hours before the scheduled departure time",
                    new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC, BaseColor.DARK_GRAY));
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
            return outputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error generating boarding pass PDF: " + e.getMessage());
        }
    }

    private PdfPCell createLabelCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(3);
        return cell;
    }

    private PdfPCell createValueCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(3);
        return cell;
    }

    private Image generateQRCodeImage(String data) throws Exception {
        int width = 100;
        int height = 100;

        BitMatrix bitMatrix = new MultiFormatWriter().encode(data, BarcodeFormat.QR_CODE, width, height);
        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "png", pngOutputStream);

        Image qrImage = Image.getInstance(pngOutputStream.toByteArray());
        qrImage.scaleToFit(80, 80); // Resize as needed
        return qrImage;
    }

    private String generateRandomSeatNumber() {
        // Generate a random seat like 12A, 23B, etc.
        Random random = new Random();
        int row = random.nextInt(30) + 1;
        char seat = (char) (random.nextInt(6) + 'A');
        return row + String.valueOf(seat);
    }

    private String generateRandomGate() {
        // Generate a random gate like A12, B5, etc.
        Random random = new Random();
        char terminal = (char) (random.nextInt(6) + 'A');
        int gate = random.nextInt(20) + 1;
        return terminal + String.valueOf(gate);
    }

    private void sendBoardingPassEmail(String email, CheckIn checkIn, FlightDto flight) {
        String flightNumber = flight != null ? flight.getFlightNumber() : "FL" + checkIn.getFlightId();
        String source = flight != null ? flight.getSource() : "Source";
        String destination = flight != null ? flight.getDestination() : "Destination";

        String subject = "Your Boarding Pass for Flight " + flightNumber;

        String body = "<html><body>" +
                "<h2>Your Boarding Pass is Ready</h2>" +
                "<p>Thank you for checking in for your flight from " + source + " to " + destination + ".</p>" +
                "<p>Please find your boarding pass attached to this email.</p>" +
                "<p>Have a great flight!</p>" +
                "<p>Swift Airlines</p>" +
                "</body></html>";

        try {
            emailService.sendEmailWithAttachment(
                    email,
                    subject,
                    body,
                    checkIn.getBoardingPassPdf(),
                    "BoardingPass_" + flightNumber + ".pdf"
            );
            log.info("Boarding pass email sent successfully to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send boarding pass email: {}", e.getMessage());
            // Don't throw exception to avoid disrupting the check-in process
        }
    }

}