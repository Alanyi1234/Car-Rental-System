package com.example.carrental.service;

import com.example.carrental.model.Reservation;
import com.example.carrental.repository.ReservationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Basic operations on reservations. Goes through ReservationRepository for all data access.
 */
@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;

    public ReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public List<Reservation> findAll() {
        // TODO
        throw new UnsupportedOperationException("TODO: findAll");
    }

    public Optional<Reservation> findById(UUID id) {
        // TODO
        throw new UnsupportedOperationException("TODO: findById");
    }

    public List<Reservation> findActiveByCarId(String carId) {
        // TODO
        throw new UnsupportedOperationException("TODO: findActiveByCarId");
    }

    public Reservation create(Reservation reservation) {
        // TODO
        throw new UnsupportedOperationException("TODO: create");
    }

    public Reservation update(Reservation reservation) {
        // TODO
        throw new UnsupportedOperationException("TODO: update");
    }

    public void delete(UUID id) {
        // TODO
        throw new UnsupportedOperationException("TODO: delete");
    }
}
