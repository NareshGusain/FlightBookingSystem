package com.flight.flightservice;

import com.flight.flightservice.exception.DuplicateFlightException;
import com.flight.flightservice.exception.InvalidInputException;
import com.flight.flightservice.model.Flight;
import com.flight.flightservice.repository.FlightRepository;
import com.flight.flightservice.service.FlightServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


//FlightServiceImplTest: Tests all service methods including:

//Saving flights with validation rules
//Getting flights by ID
//Getting all flights
//Reducing seats
//Searching flights by date

@ExtendWith(MockitoExtension.class)
public class FlightServiceImplTest {

    @Mock
    private FlightRepository flightRepository;

    @InjectMocks
    private FlightServiceImpl flightService;

    private Flight validFlight;
    private LocalDateTime validDepartureTime;
    private LocalDateTime validArrivalTime;

    @BeforeEach
    void setUp() {
        validDepartureTime = LocalDateTime.of(2025, 4, 20, 10, 30);
        validArrivalTime = LocalDateTime.of(2025, 4, 20, 12, 30);

        validFlight = new Flight();
        validFlight.setFlightId(1L);
        validFlight.setFlightNumber("AI101");
        validFlight.setSource("Delhi");
        validFlight.setDestination("Mumbai");
        validFlight.setDepartureDateTime(validDepartureTime);
        validFlight.setArrivalDateTime(validArrivalTime);
        validFlight.setCapacity(180);
        validFlight.setAvailableSeats(150);
        validFlight.setCost(5000.0);
    }

    @Test
    @DisplayName("Should save valid flight successfully")
    void testSaveValidFlight() {
        // Arrange
        when(flightRepository.existsByFlightNumberAndDepartureDateTimeBetween(
                anyString(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(false);
        when(flightRepository.save(any(Flight.class))).thenReturn(validFlight);

        // Act
        Flight savedFlight = flightService.saveFlight(validFlight);

        // Assert
        assertNotNull(savedFlight);
        assertEquals(validFlight.getFlightNumber(), savedFlight.getFlightNumber());
        verify(flightRepository, times(1)).save(validFlight);
    }

    @Test
    @DisplayName("Should throw exception when source and destination are same")
    void testSaveFlightWithSameSourceAndDestination() {
        // Arrange
        Flight invalidFlight = new Flight();
        invalidFlight.setFlightNumber("AI101");
        invalidFlight.setSource("Delhi");
        invalidFlight.setDestination("Delhi");
        invalidFlight.setDepartureDateTime(validDepartureTime);
        invalidFlight.setArrivalDateTime(validArrivalTime);
        invalidFlight.setCapacity(180);
        invalidFlight.setAvailableSeats(150);
        invalidFlight.setCost(5000.0);

        // Act & Assert
        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> flightService.saveFlight(invalidFlight));
        assertEquals("Flight cannot have same source and destination", exception.getMessage());
        verify(flightRepository, never()).save(any(Flight.class));
    }

    @Test
    @DisplayName("Should throw exception when negative capacity")
    void testSaveFlightWithNegativeCapacity() {
        // Arrange
        Flight invalidFlight = new Flight();
        invalidFlight.setFlightNumber("AI101");
        invalidFlight.setSource("Delhi");
        invalidFlight.setDestination("Mumbai");
        invalidFlight.setDepartureDateTime(validDepartureTime);
        invalidFlight.setArrivalDateTime(validArrivalTime);
        invalidFlight.setCapacity(-10);  // Negative capacity
        invalidFlight.setAvailableSeats(150);
        invalidFlight.setCost(5000.0);

        // Act & Assert
        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> flightService.saveFlight(invalidFlight));
        assertEquals("Invalid flight details: Capacity and cost must be positive", exception.getMessage());
        verify(flightRepository, never()).save(any(Flight.class));
    }

    @Test
    @DisplayName("Should throw exception when arrival time is before departure time")
    void testSaveFlightWithInvalidTimes() {
        // Arrange
        LocalDateTime earlierTime = LocalDateTime.of(2025, 4, 20, 13, 30);
        LocalDateTime laterTime = LocalDateTime.of(2025, 4, 20, 10, 30);

        Flight invalidFlight = new Flight();
        invalidFlight.setFlightNumber("AI101");
        invalidFlight.setSource("Delhi");
        invalidFlight.setDestination("Mumbai");
        invalidFlight.setDepartureDateTime(earlierTime);
        invalidFlight.setArrivalDateTime(laterTime);  // Arrival before departure
        invalidFlight.setCapacity(180);
        invalidFlight.setAvailableSeats(150);
        invalidFlight.setCost(5000.0);

        // Act & Assert
        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> flightService.saveFlight(invalidFlight));
        assertEquals("Arrival time must be after departure time", exception.getMessage());
        verify(flightRepository, never()).save(any(Flight.class));
    }

    @Test
    @DisplayName("Should throw exception when flight with same number exists on the same day")
    void testSaveDuplicateFlight() {
        // Arrange
        when(flightRepository.existsByFlightNumberAndDepartureDateTimeBetween(
                anyString(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(true);

        // Act & Assert
        DuplicateFlightException exception = assertThrows(DuplicateFlightException.class,
                () -> flightService.saveFlight(validFlight));
        assertTrue(exception.getMessage().contains("Flight with number AI101 already exists"));
        verify(flightRepository, never()).save(any(Flight.class));
    }

    @Test
    @DisplayName("Should get all flights")
    void testGetAllFlights() {
        // Arrange
        List<Flight> flightList = Arrays.asList(validFlight);
        when(flightRepository.findAll()).thenReturn(flightList);

        // Act
        List<Flight> result = flightService.getAllFlights();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(flightRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should get flight by ID")
    void testGetFlightById() {
        // Arrange
        when(flightRepository.findById(1L)).thenReturn(Optional.of(validFlight));

        // Act
        Flight result = flightService.getFlightById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(validFlight.getFlightNumber(), result.getFlightNumber());
        verify(flightRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when flight not found by ID")
    void testGetFlightByIdNotFound() {
        // Arrange
        when(flightRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> flightService.getFlightById(999L));
        assertEquals("Flight not found with ID: 999", exception.getMessage());
    }

    @Test
    @DisplayName("Should reduce seats when enough seats available")
    void testReduceSeatsSuccess() {
        // Arrange
        Flight flight = new Flight();
        flight.setFlightId(1L);
        flight.setAvailableSeats(50);
        when(flightRepository.findById(1L)).thenReturn(Optional.of(flight));
        when(flightRepository.save(any(Flight.class))).thenReturn(flight);

        // Act
        Flight result = flightService.reduceSeats(1L, 5);

        // Assert
        assertNotNull(result);
        assertEquals(45, result.getAvailableSeats());
        verify(flightRepository, times(1)).save(flight);
    }

    @Test
    @DisplayName("Should return null when not enough seats available")
    void testReduceSeatsInsufficientSeats() {
        // Arrange
        Flight flight = new Flight();
        flight.setFlightId(1L);
        flight.setAvailableSeats(3);
        when(flightRepository.findById(1L)).thenReturn(Optional.of(flight));

        // Act
        Flight result = flightService.reduceSeats(1L, 5);

        // Assert
        assertNull(result);
        verify(flightRepository, never()).save(any(Flight.class));
    }

    @Test
    @DisplayName("Should search flights by source, destination and date")
    void testSearchFlightsByDate() {
        // Arrange
        LocalDateTime searchDate = LocalDateTime.of(2025, 4, 20, 0, 0);
        LocalDateTime startOfDay = searchDate.withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = searchDate.withHour(23).withMinute(59).withSecond(59).withNano(999999999);

        List<Flight> expectedFlights = Arrays.asList(validFlight);

        when(flightRepository.findBySourceAndDestinationAndDepartureDateTimeBetween(
                "Delhi", "Mumbai", startOfDay, endOfDay))
                .thenReturn(expectedFlights);

        // Act
        List<Flight> result = flightService.searchFlightsByDate("Delhi", "Mumbai", searchDate);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("AI101", result.get(0).getFlightNumber());
        verify(flightRepository, times(1)).findBySourceAndDestinationAndDepartureDateTimeBetween(
                anyString(), anyString(), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should throw exception when search with null parameters")
    void testSearchFlightsWithNullParameters() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> flightService.searchFlightsByDate(null, "Mumbai", LocalDateTime.now()));
        assertThrows(IllegalArgumentException.class,
                () -> flightService.searchFlightsByDate("Delhi", null, LocalDateTime.now()));
        assertThrows(IllegalArgumentException.class,
                () -> flightService.searchFlightsByDate("Delhi", "Mumbai", null));
    }

    @Test
    @DisplayName("Should throw exception when search with empty parameters")
    void testSearchFlightsWithEmptyParameters() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> flightService.searchFlightsByDate("", "Mumbai", LocalDateTime.now()));
        assertThrows(IllegalArgumentException.class,
                () -> flightService.searchFlightsByDate("Delhi", "", LocalDateTime.now()));
    }
}