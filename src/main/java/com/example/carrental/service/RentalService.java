package com.example.carrental.service;

import com.example.carrental.domain.Car;
import com.example.carrental.domain.CarType;
import com.example.carrental.domain.Fleet;
import com.example.carrental.domain.NoAvailabilityException;
import com.example.carrental.domain.Reservation;
import com.example.carrental.domain.ReservationCancelledException;
import com.example.carrental.domain.ReservationNotFoundException;
import com.example.carrental.repository.ReservationRepository;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Core booking logic. Plain Java with no framework dependencies.
 *
 * <p>Concurrency: every operation that reads-then-writes availability is {@code synchronized},
 * so two concurrent requests can never both take the last car. This is correct for a single
 * instance; with several app servers, the database constraint in docs/schema.sql takes over.
 */
public class RentalService {

    private final Fleet fleet;
    private final ReservationRepository repository;
    private final Clock clock;

    public RentalService(Fleet fleet, ReservationRepository repository, Clock clock) {
        this.fleet = Objects.requireNonNull(fleet);
        this.repository = Objects.requireNonNull(repository);
        this.clock = Objects.requireNonNull(clock);
    }

    /**
     * Reserves a car of the given type for {@code days} days starting at {@code start}.
     *
     * @throws IllegalArgumentException if the input is invalid
     * @throws NoAvailabilityException  if every car of that type is booked during the period
     */
    public synchronized Reservation reserve(CarType type, LocalDateTime start, int days) {
        if (type == null) {
            throw new IllegalArgumentException("carType is required");
        }
        validateStart(start);
        validateDays(days);

        LocalDateTime end = start.plusDays(days);
        Car car = findFreeCar(type, start, end, null)
                .orElseThrow(() -> new NoAvailabilityException(type, start, end));

        Reservation reservation = Reservation.create(car, start, days);
        repository.save(reservation);
        return reservation;
    }

    public Reservation getReservation(UUID id) {
        return repository.findById(id).orElseThrow(() -> new ReservationNotFoundException(id));
    }

    /**
     * Changes the type, start and/or length of an active reservation. A null argument means
     * "keep the current value". Keeps the same car when possible, otherwise switches to another
     * free car of the requested type. If nothing fits, the original reservation is left unchanged.
     *
     * @throws ReservationCancelledException if the reservation is cancelled
     */
    public synchronized Reservation modify(UUID id, CarType newType, LocalDateTime newStart, Integer newDays) {
        Reservation current = getReservation(id);
        if (!current.isActive()) {
            throw new ReservationCancelledException(id);
        }

        CarType type = newType != null ? newType : current.carType();
        LocalDateTime start = current.start();
        if (newStart != null) {
            validateStart(newStart); // only a *new* start must be in the future
            start = newStart;
        }
        int days = newDays != null ? newDays : current.days();
        validateDays(days);

        LocalDateTime end = start.plusDays(days);

        Optional<Car> car = Optional.empty();
        if (type == current.carType() && isFree(current.car(), start, end, id)) {
            car = Optional.of(current.car());
        }
        if (car.isEmpty()) {
            car = findFreeCar(type, start, end, id);
        }

        LocalDateTime finalStart = start;
        Reservation updated = current.reschedule(
                car.orElseThrow(() -> new NoAvailabilityException(type, finalStart, end)), start, days);
        repository.save(updated);
        return updated;
    }

    /**
     * Cancels a reservation, freeing its car. Idempotent: cancelling an already-cancelled
     * reservation does nothing.
     */
    public synchronized void cancel(UUID id) {
        Reservation reservation = getReservation(id);
        if (reservation.isActive()) {
            repository.save(reservation.cancel());
        }
    }

    /**
     * Number of free cars per type for the period. A snapshot only: nothing is held, so a
     * later {@link #reserve} can still fail if someone else books first.
     */
    public synchronized Map<CarType, Integer> availability(LocalDateTime start, int days) {
        validateStart(start);
        validateDays(days);
        LocalDateTime end = start.plusDays(days);

        Map<CarType, Integer> result = new EnumMap<>(CarType.class);
        for (CarType type : CarType.values()) {
            int free = (int) fleet.carsOf(type).stream()
                    .filter(car -> isFree(car, start, end, null))
                    .count();
            result.put(type, free);
        }
        return result;
    }

    // --- helpers -----------------------------------------------------------------------------

    private Optional<Car> findFreeCar(CarType type, LocalDateTime start, LocalDateTime end, UUID ignoreReservationId) {
        return fleet.carsOf(type).stream()
                .filter(car -> isFree(car, start, end, ignoreReservationId))
                .findFirst();
    }

    /** True if the car has no active reservation overlapping [start, end), ignoring the given one. */
    private boolean isFree(Car car, LocalDateTime start, LocalDateTime end, UUID ignoreReservationId) {
        return repository.findActiveByCarId(car.id()).stream()
                .filter(r -> !r.id().equals(ignoreReservationId))
                .noneMatch(r -> r.overlaps(start, end));
    }

    private void validateStart(LocalDateTime start) {
        if (start == null) {
            throw new IllegalArgumentException("start is required");
        }
        if (start.isBefore(LocalDateTime.now(clock))) {
            throw new IllegalArgumentException("start must not be in the past");
        }
    }

    private static void validateDays(int days) {
        if (days < 1) {
            throw new IllegalArgumentException("days must be at least 1");
        }
    }
}
