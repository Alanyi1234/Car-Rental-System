package com.example.carrental.domain;

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

    public static Reservation create(Car car, LocalDateTime start, int days) {
        // TODO
        throw new UnsupportedOperationException("TODO: create");
    }

    public CarType carType() {
        return car.type();
    }

    public int days() {
        // TODO
        throw new UnsupportedOperationException("TODO: days");
    }

    public boolean isActive() {
        return status == ReservationStatus.ACTIVE;
    }

    /** True if this reservation's period overlaps [otherStart, otherEnd). */
    public boolean overlaps(LocalDateTime otherStart, LocalDateTime otherEnd) {
        // TODO
        throw new UnsupportedOperationException("TODO: overlaps");
    }

    public Reservation cancel() {
        // TODO
        throw new UnsupportedOperationException("TODO: cancel");
    }

    public Reservation reschedule(Car newCar, LocalDateTime newStart, int newDays) {
        // TODO
        throw new UnsupportedOperationException("TODO: reschedule");
    }
}
