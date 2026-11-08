package com.example.carrental.service;

import com.example.carrental.domain.CarType;
import com.example.carrental.domain.Fleet;
import com.example.carrental.domain.Reservation;
import com.example.carrental.repository.ReservationRepository;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Core booking logic. Plain Java with no framework dependencies.
 */
public class RentalService {

    private final Fleet fleet;
    private final ReservationRepository repository;
    private final Clock clock;

    public RentalService(Fleet fleet, ReservationRepository repository, Clock clock) {
        this.fleet = fleet;
        this.repository = repository;
        this.clock = clock;
    }

    /** Reserves a car of the given type for {@code days} days starting at {@code start}. */
    public Reservation reserve(CarType type, LocalDateTime start, int days) {
        // TODO
        throw new UnsupportedOperationException("TODO: reserve");
    }

    /** Looks up a reservation by id. */
    public Reservation getReservation(UUID id) {
        // TODO
        throw new UnsupportedOperationException("TODO: getReservation");
    }

    /** Changes the type, start and/or length of a reservation. A null argument means "keep the current value". */
    public Reservation modify(UUID id, CarType newType, LocalDateTime newStart, Integer newDays) {
        // TODO
        throw new UnsupportedOperationException("TODO: modify");
    }

    /** Cancels a reservation, freeing its car. */
    public void cancel(UUID id) {
        // TODO
        throw new UnsupportedOperationException("TODO: cancel");
    }

    /** Number of free cars per type for the period. */
    public Map<CarType, Integer> availability(LocalDateTime start, int days) {
        // TODO
        throw new UnsupportedOperationException("TODO: availability");
    }
}
