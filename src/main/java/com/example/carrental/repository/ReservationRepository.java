package com.example.carrental.repository;

import com.example.carrental.model.Reservation;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Data access for reservations.
 */
@Repository
public class ReservationRepository {

    public List<Reservation> findAll() {
        // TODO
        throw new UnsupportedOperationException("TODO: findAll");
    }

    public void deleteById(UUID id) {
        // TODO
        throw new UnsupportedOperationException("TODO: deleteById");
    }

    /**
     * Inserts or updates a reservation. Throws DataIntegrityViolationException if the database
     * constraint rejects it because it overlaps another active booking of the same car.
     */
    public void save(Reservation reservation) {
        // TODO
        throw new UnsupportedOperationException("TODO: save");
    }

    public Optional<Reservation> findById(UUID id) {
        // TODO
        throw new UnsupportedOperationException("TODO: findById");
    }

    /** Non-cancelled reservations for one car. */
    public List<Reservation> findActiveByCarId(UUID carId) {
        // TODO
        throw new UnsupportedOperationException("TODO: findActiveByCarId");
    }
}
