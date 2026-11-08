package com.example.carrental.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Body of POST /reservations.
 */
public record ReservationRequest(
        @NotNull CarType carType,
        @NotNull LocalDateTime start,
        @NotNull @Min(1) Integer days
) {
}
