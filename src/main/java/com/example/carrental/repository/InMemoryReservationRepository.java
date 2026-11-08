package com.example.carrental.repository;

import com.example.carrental.model.Reservation;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryReservationRepository implements ReservationRepository {

    private final Map<UUID, Reservation> reservations = new ConcurrentHashMap<>();

    @Override
    public void save(Reservation reservation) {
        // TODO
        throw new UnsupportedOperationException("TODO: save");
    }

    @Override
    public Optional<Reservation> findById(UUID id) {
        // TODO
        throw new UnsupportedOperationException("TODO: findById");
    }

    @Override
    public List<Reservation> findActiveByCarId(String carId) {
        // TODO
        throw new UnsupportedOperationException("TODO: findActiveByCarId");
    }
}
