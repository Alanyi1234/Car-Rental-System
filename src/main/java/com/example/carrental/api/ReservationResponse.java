package com.example.carrental.api;

import com.example.carrental.domain.CarType;
import com.example.carrental.domain.Reservation;
import com.example.carrental.domain.ReservationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * API view of a reservation. Kept separate from the domain object so internal changes
 * don't leak into the public contract.
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

    public static ReservationResponse from(Reservation r) {
        return new ReservationResponse(r.id(), r.car().id(), r.carType(), r.start(), r.end(), r.days(), r.status());
    }
}
