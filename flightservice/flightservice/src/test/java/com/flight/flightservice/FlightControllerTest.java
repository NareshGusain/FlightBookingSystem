package com.flight.flightservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.flight.flightservice.controller.FlightController;
import com.flight.flightservice.model.Flight;
import com.flight.flightservice.service.FlightService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;



@WebMvcTest(FlightController.class)
public class FlightControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FlightService flightService;

    private ObjectMapper objectMapper;
    private Flight testFlight;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        testFlight = new Flight();
        testFlight.setFlightId(1L);
        testFlight.setFlightNumber("AI101");
        testFlight.setSource("Delhi");
        testFlight.setDestination("Mumbai");
        testFlight.setDepartureDateTime(LocalDateTime.of(2025, 4, 20, 10, 30));
        testFlight.setArrivalDateTime(LocalDateTime.of(2025, 4, 20, 12, 30));
        testFlight.setCapacity(180);
        testFlight.setAvailableSeats(150);
        testFlight.setCost(5000.0);
    }

    @Test
    @DisplayName("Should create flight")
    void testCreateFlight() throws Exception {
        when(flightService.saveFlight(any(Flight.class))).thenReturn(testFlight);

        mockMvc.perform(post("/flights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testFlight)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flightNumber", is("AI101")))
                .andExpect(jsonPath("$.source", is("Delhi")))
                .andExpect(jsonPath("$.destination", is("Mumbai")));

        verify(flightService, times(1)).saveFlight(any(Flight.class));
    }

    @Test
    @DisplayName("Should get all flights")
    void testGetAllFlights() throws Exception {
        when(flightService.getAllFlights()).thenReturn(Arrays.asList(testFlight));

        mockMvc.perform(get("/flights"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].flightNumber", is("AI101")));

        verify(flightService, times(1)).getAllFlights();
    }

    @Test
    @DisplayName("Should get flight by ID")
    void testGetFlightById() throws Exception {
        when(flightService.getFlightById(1L)).thenReturn(testFlight);

        mockMvc.perform(get("/flights/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flightId", is(1)))
                .andExpect(jsonPath("$.flightNumber", is("AI101")));

        verify(flightService, times(1)).getFlightById(1L);
    }

    @Test
    @DisplayName("Should reduce available seats")
    void testReduceAvailableSeats() throws Exception {
        Flight updatedFlight = new Flight();
        updatedFlight.setFlightId(1L);
        updatedFlight.setFlightNumber("AI101");
        updatedFlight.setAvailableSeats(145);  // 5 seats reduced from 150

        when(flightService.reduceSeats(eq(1L), eq(5))).thenReturn(updatedFlight);

        mockMvc.perform(put("/flights/1/reduce-seats")
                        .param("passengers", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableSeats", is(145)));

        verify(flightService, times(1)).reduceSeats(1L, 5);
    }

    @Test
    @DisplayName("Should return 404 when reducing seats on non-existent flight")
    void testReduceAvailableSeatsNotFound() throws Exception {
        when(flightService.reduceSeats(eq(999L), anyInt())).thenReturn(null);

        mockMvc.perform(put("/flights/999/reduce-seats")
                        .param("passengers", "5"))
                .andExpect(status().isNotFound());

        verify(flightService, times(1)).reduceSeats(999L, 5);
    }

    @Test
    @DisplayName("Should search flights by source, destination and date")
    void testSearchFlights() throws Exception {
        when(flightService.searchFlightsByDate(
                eq("Delhi"),
                eq("Mumbai"),
                any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(testFlight));

        mockMvc.perform(get("/flights/search")
                        .param("source", "Delhi")
                        .param("destination", "Mumbai")
                        .param("date", "2025-04-20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].flightNumber", is("AI101")));

        verify(flightService, times(1)).searchFlightsByDate(
                anyString(), anyString(), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should return no content when no flights found")
    void testSearchFlightsNoResults() throws Exception {
        when(flightService.searchFlightsByDate(
                anyString(),
                anyString(),
                any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/flights/search")
                        .param("source", "Delhi")
                        .param("destination", "Chennai")
                        .param("date", "2025-04-20"))
                .andExpect(status().isNoContent());

        verify(flightService, times(1)).searchFlightsByDate(
                anyString(), anyString(), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should handle invalid date format")
    void testSearchFlightsInvalidDate() throws Exception {
        when(flightService.searchFlightsByDate(
                anyString(),
                anyString(),
                any(LocalDateTime.class)))
                .thenThrow(new IllegalArgumentException("Invalid date format"));

        mockMvc.perform(get("/flights/search")
                        .param("source", "Delhi")
                        .param("destination", "Mumbai")
                        .param("date", "invalid-date"))
                .andExpect(status().isBadRequest());
    }
}