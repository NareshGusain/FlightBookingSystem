package com.flightbooking.checkinservice.repository;

import com.flightbooking.checkinservice.model.CheckIn;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CheckInRepository extends JpaRepository<CheckIn, Long> {
}
