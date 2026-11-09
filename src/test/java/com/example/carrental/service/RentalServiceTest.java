package com.example.carrental.service;

import com.example.carrental.model.Car;
import com.example.carrental.model.CarType;
import com.example.carrental.model.Reservation;
import com.example.carrental.model.ReservationStatus;
import com.example.carrental.repository.CarRepository;
import com.example.carrental.repository.ReservationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Basic unit tests for RentalService, with both repositories mocked by Mockito.
 */
@ExtendWith(MockitoExtension.class)
class RentalServiceTest {

    private static final LocalDateTime START = LocalDateTime.of(2030, 12, 1, 10, 0);

    @Mock
    private CarRepository carRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private RentalService rentalService;

    private final UUID userId = UUID.randomUUID();
    private final Car suv = new Car(UUID.randomUUID(), CarType.SUV);

    @Test
    void reserve_savesReservationWhenCarIsFree() {
        when(carRepository.findByType(CarType.SUV)).thenReturn(List.of(suv));
        when(reservationRepository.findActiveByCarId(suv.id())).thenReturn(List.of());

        Reservation result = rentalService.reserve(userId, CarType.SUV, START, 3);

        assertEquals(suv, result.car());
        assertEquals(userId, result.userId());
        assertEquals(ReservationStatus.ACTIVE, result.status());
        verify(reservationRepository).save(result);
    }

    @Test
    void reserve_throwsWhenNoCarIsFree() {
        Reservation existing = new Reservation(UUID.randomUUID(), userId, suv, START, START.plusDays(3), ReservationStatus.ACTIVE);
        when(carRepository.findByType(CarType.SUV)).thenReturn(List.of(suv));
        when(reservationRepository.findActiveByCarId(suv.id())).thenReturn(List.of(existing));

        assertThrows(IllegalStateException.class, () -> rentalService.reserve(userId, CarType.SUV, START, 3));
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void getReservation_throwsWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(reservationRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> rentalService.getReservation(id));
    }

    @Test
    void cancel_savesReservationAsCancelled() {
        Reservation existing = new Reservation(UUID.randomUUID(), userId, suv, START, START.plusDays(3), ReservationStatus.ACTIVE);
        when(reservationRepository.findById(existing.id())).thenReturn(Optional.of(existing));

        rentalService.cancel(existing.id());

        ArgumentCaptor<Reservation> saved = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationRepository).save(saved.capture());
        assertEquals(ReservationStatus.CANCELLED, saved.getValue().status());
    }
}
