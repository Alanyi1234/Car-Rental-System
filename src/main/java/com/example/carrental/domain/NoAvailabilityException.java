package com.example.carrental.domain;

import java.time.LocalDateTime;

/**
 * Thrown when no car of the requested type is free for the whole requested period.
 */
public class NoAvailabilityException extends RuntimeException {

    public NoAvailabilityException(CarType type, LocalDateTime start, LocalDateTime end) {
        super("No " + type + " available from " + start + " to " + end);
    }
}
