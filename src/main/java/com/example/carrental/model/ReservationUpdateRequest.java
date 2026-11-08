package com.example.carrental.model;

import jakarta.validation.constraints.Min;

import java.time.LocalDateTime;

/**
 * Body of PATCH /reservations/{id}. Every field is optional; omitted fields keep their value.
 */
public record ReservationUpdateRequest(
        CarType carType,
        LocalDateTime start,
        @Min(1) Integer days
) {
}
