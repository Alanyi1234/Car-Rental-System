package com.example.carrental.repository;

import com.example.carrental.domain.Reservation;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryReservationRepository implements ReservationRepository {

    private final Map<UUID, Reservation> reservations = new ConcurrentHashMap<>();

    @Override
    public void save(Reservation reservation) {
        reservations.put(reservation.id(), reservation);
    }

    @Override
    public Optional<Reservation> findById(UUID id) {
        return Optional.ofNullable(reservations.get(id));
    }

    @Override
    public List<Reservation> findActiveByCarId(String carId) {
        // Linear scan is fine at this size; a database would use an index on car_id.
        return reservations.values().stream()
                .filter(Reservation::isActive)
                .filter(r -> r.car().id().equals(carId))
                .toList();
    }
}
