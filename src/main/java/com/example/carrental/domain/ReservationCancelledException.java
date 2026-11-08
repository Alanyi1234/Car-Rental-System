package com.example.carrental.domain;

import java.util.UUID;

/**
 * Thrown when trying to modify a reservation that has already been cancelled.
 */
public class ReservationCancelledException extends RuntimeException {

    public ReservationCancelledException(UUID id) {
        super("Reservation " + id + " is cancelled and cannot be modified");
    }
}
