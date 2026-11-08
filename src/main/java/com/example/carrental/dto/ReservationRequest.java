package com.example.carrental.dto;

import com.example.carrental.domain.CarType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Body of POST /reservations. The "start must not be in the past" rule is checked in the
 * service (using the injected Clock), not here, so there is a single source of "now".
 */
public record ReservationRequest(
        @NotNull CarType carType,
        @NotNull LocalDateTime start,
        @NotNull @Min(1) Integer days
) {
}
