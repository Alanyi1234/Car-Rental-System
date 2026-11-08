package com.example.carrental.service;

import com.example.carrental.model.CarType;
import com.example.carrental.model.Reservation;
import com.example.carrental.repository.CarRepository;
import com.example.carrental.repository.ReservationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Core booking logic.
 */
@Service
public class RentalService {

    private final CarRepository carRepository;
    private final ReservationRepository reservationRepository;

    public RentalService(CarRepository carRepository, ReservationRepository reservationRepository) {
        this.carRepository = carRepository;
        this.reservationRepository = reservationRepository;
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
