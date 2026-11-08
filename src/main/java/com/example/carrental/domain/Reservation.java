package com.example.carrental.domain;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * A booking of one specific car for the half-open period [start, end).
 *
 * <p>Immutable: changes produce a new instance, which keeps the service logic
 * free of partially-updated state if a modification is rejected.
 */
public record Reservation(
        UUID id,
        Car car,
        LocalDateTime start,
        LocalDateTime end,
        ReservationStatus status
) {

    public Reservation {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(car, "car");
        Objects.requireNonNull(start, "start");
        Objects.requireNonNull(end, "end");
        Objects.requireNonNull(status, "status");
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("end must be after start");
        }
    }

    public static Reservation create(Car car, LocalDateTime start, int days) {
        return new Reservation(UUID.randomUUID(), car, start, start.plusDays(days), ReservationStatus.ACTIVE);
    }

    public CarType carType() {
        return car.type();
    }

    public int days() {
        return (int) Duration.between(start, end).toDays();
    }

    public boolean isActive() {
        return status == ReservationStatus.ACTIVE;
    }

    /**
     * Two half-open periods [a, b) and [c, d) overlap iff a < d and c < b.
     * Back-to-back bookings (one ends at 10:00, the next starts at 10:00) do not overlap.
     */
    public boolean overlaps(LocalDateTime otherStart, LocalDateTime otherEnd) {
        return start.isBefore(otherEnd) && otherStart.isBefore(end);
    }

    public Reservation cancel() {
        return new Reservation(id, car, start, end, ReservationStatus.CANCELLED);
    }

    public Reservation reschedule(Car newCar, LocalDateTime newStart, int newDays) {
        return new Reservation(id, newCar, newStart, newStart.plusDays(newDays), status);
    }
}
