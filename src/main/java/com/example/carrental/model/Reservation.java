package com.example.carrental.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A booking of one specific car for the period [start, end).
 */
public record Reservation(
        UUID id,
        Car car,
        LocalDateTime start,
        LocalDateTime end,
        ReservationStatus status
) {
}
