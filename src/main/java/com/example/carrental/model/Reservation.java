package com.example.carrental.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A booking of one specific car for the period [start, end), made by one user.
 * {@code userId} is the user's id from their JWT ({@code sub} claim).
 */
public record Reservation(
        UUID id,
        UUID userId,
        Car car,
        LocalDateTime start,
        LocalDateTime end,
        ReservationStatus status
) {
}
