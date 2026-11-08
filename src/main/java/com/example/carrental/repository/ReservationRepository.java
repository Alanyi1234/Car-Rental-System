package com.example.carrental.repository;

import com.example.carrental.model.Reservation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Storage abstraction for reservations. The in-memory implementation is used for this
 * exercise; a database-backed implementation could be swapped in without touching the service.
 */
public interface ReservationRepository {

    /** Inserts or replaces the reservation with the same id. */
    void save(Reservation reservation);

    Optional<Reservation> findById(UUID id);

    List<Reservation> findActiveByCarId(String carId);
}
