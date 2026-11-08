package com.example.carrental.service;

import com.example.carrental.model.Car;
import com.example.carrental.model.CarType;
import com.example.carrental.model.Reservation;
import com.example.carrental.model.ReservationStatus;
import com.example.carrental.repository.CarRepository;
import com.example.carrental.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

/**
 * Booking logic (reserve, modify, cancel, availability). All data access goes through
 * CarRepository and ReservationRepository.
 *
 * <p>Concurrency: reserve, modify and cancel each run in one transaction. Booking reads the
 * cars of the requested type with a row lock, so two requests for the same type run one after
 * the other and can't both take the last free car.
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
    @Transactional
    public Reservation reserve(CarType type, LocalDateTime start, int days) {
        LocalDateTime end = start.plusDays(days);

        // Lock the cars of this type until the transaction ends.
        List<Car> cars = carRepository.findByTypeForUpdate(type);
        Car car = firstFreeCar(cars, start, end, null)
                .orElseThrow(() -> new IllegalStateException("No " + type + " available for that period"));

        Reservation reservation = new Reservation(UUID.randomUUID(), car, start, end, ReservationStatus.ACTIVE);
        reservationRepository.save(reservation);
        return reservation;
    }

    /** Looks up a reservation by id. */
    public Reservation getReservation(UUID id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Reservation " + id + " not found"));
    }

    /** Changes the type, start and/or length of a reservation. A null argument means "keep the current value". */
    @Transactional
    public Reservation modify(UUID id, CarType newType, LocalDateTime newStart, Integer newDays) {
        Reservation current = getReservation(id);
        if (current.status() == ReservationStatus.CANCELLED) {
            throw new IllegalStateException("Reservation " + id + " is cancelled");
        }

        CarType type = newType != null ? newType : current.car().type();
        LocalDateTime start = newStart != null ? newStart : current.start();
        int days = newDays != null ? newDays : (int) Duration.between(current.start(), current.end()).toDays();
        LocalDateTime end = start.plusDays(days);

        // Lock the cars of the requested type until the transaction ends.
        List<Car> cars = carRepository.findByTypeForUpdate(type);

        // Keep the same car if it is still free, otherwise pick another free car of the type.
        Optional<Car> car = Optional.empty();
        if (type == current.car().type() && isFree(current.car(), start, end, id)) {
            car = Optional.of(current.car());
        }
        if (car.isEmpty()) {
            car = firstFreeCar(cars, start, end, id);
        }

        Reservation updated = new Reservation(
                id,
                car.orElseThrow(() -> new IllegalStateException("No " + type + " available for that period")),
                start, end, ReservationStatus.ACTIVE);
        reservationRepository.save(updated);
        return updated;
    }

    /** Cancels a reservation, freeing its car. Cancelling twice has no extra effect. */
    @Transactional
    public void cancel(UUID id) {
        Reservation reservation = getReservation(id);
        if (reservation.status() == ReservationStatus.ACTIVE) {
            reservationRepository.save(new Reservation(
                    reservation.id(), reservation.car(), reservation.start(), reservation.end(),
                    ReservationStatus.CANCELLED));
        }
    }

    /** Number of free cars per type for the period. */
    public Map<CarType, Integer> availability(LocalDateTime start, int days) {
        LocalDateTime end = start.plusDays(days);

        Map<CarType, Integer> result = new EnumMap<>(CarType.class);
        for (CarType type : CarType.values()) {
            int free = (int) carRepository.findByType(type).stream()
                    .filter(car -> isFree(car, start, end, null))
                    .count();
            result.put(type, free);
        }
        return result;
    }

    /** The individual cars that are free for the period. With no type (null), returns free cars of every type. */
    public List<Car> availableCars(LocalDateTime start, int days, CarType type) {
        LocalDateTime end = start.plusDays(days);

        List<Car> cars = type != null ? carRepository.findByType(type) : carRepository.findAll();
        return cars.stream()
                .filter(car -> isFree(car, start, end, null))
                .toList();
    }

    // --- helpers ---------------------------------------------------------------------------

    /** First car in the list with no overlapping active reservation. */
    private Optional<Car> firstFreeCar(List<Car> cars, LocalDateTime start, LocalDateTime end, UUID ignoreReservationId) {
        return cars.stream()
                .filter(car -> isFree(car, start, end, ignoreReservationId))
                .findFirst();
    }

    /** True if none of the car's active reservations overlap [start, end), ignoring the given one. */
    private boolean isFree(Car car, LocalDateTime start, LocalDateTime end, UUID ignoreReservationId) {
        return reservationRepository.findActiveByCarId(car.id()).stream()
                .filter(r -> !r.id().equals(ignoreReservationId))
                .noneMatch(r -> r.start().isBefore(end) && start.isBefore(r.end()));
    }
}
