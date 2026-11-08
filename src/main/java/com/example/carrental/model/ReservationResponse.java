package com.example.carrental.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * API view of a reservation, returned by the reservation endpoints.
 */
public record ReservationResponse(
        UUID id,
        String carId,
        CarType carType,
        LocalDateTime start,
        LocalDateTime end,
        int days,
        ReservationStatus status
) {
}
