package com.example.carrental.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationTest {

    private static final Car SUV_1 = new Car("SUV-1", CarType.SUV);
    private static final LocalDateTime DEC_1 = LocalDateTime.of(2030, 12, 1, 10, 0);

    // Reservation under test covers [Dec 1 10:00, Dec 4 10:00)
    private final Reservation reservation = Reservation.create(SUV_1, DEC_1, 3);

    @Test
    void createComputesEndFromDays() {
        assertThat(reservation.end()).isEqualTo(DEC_1.plusDays(3));
        assertThat(reservation.days()).isEqualTo(3);
        assertThat(reservation.status()).isEqualTo(ReservationStatus.ACTIVE);
        assertThat(reservation.carType()).isEqualTo(CarType.SUV);
    }

    @Test
    void identicalPeriodOverlaps() {
        assertThat(reservation.overlaps(DEC_1, DEC_1.plusDays(3))).isTrue();
    }

    @Test
    void periodStartingInsideOverlaps() {
        assertThat(reservation.overlaps(DEC_1.plusDays(2), DEC_1.plusDays(5))).isTrue();
    }

    @Test
    void periodEndingInsideOverlaps() {
        assertThat(reservation.overlaps(DEC_1.minusDays(1), DEC_1.plusDays(1))).isTrue();
    }

    @Test
    void periodFullyInsideOverlaps() {
        assertThat(reservation.overlaps(DEC_1.plusHours(1), DEC_1.plusDays(1))).isTrue();
    }

    @Test
    void periodFullyContainingOverlaps() {
        assertThat(reservation.overlaps(DEC_1.minusDays(1), DEC_1.plusDays(10))).isTrue();
    }

    @Test
    void periodEndingExactlyAtStartDoesNotOverlap() {
        assertThat(reservation.overlaps(DEC_1.minusDays(2), DEC_1)).isFalse();
    }

    @Test
    void periodStartingExactlyAtEndDoesNotOverlap() {
        assertThat(reservation.overlaps(DEC_1.plusDays(3), DEC_1.plusDays(5))).isFalse();
    }

    @Test
    void periodsFarApartDoNotOverlap() {
        assertThat(reservation.overlaps(DEC_1.plusDays(10), DEC_1.plusDays(12))).isFalse();
    }

    @Test
    void cancelReturnsCancelledCopyWithSameId() {
        Reservation cancelled = reservation.cancel();

        assertThat(cancelled.id()).isEqualTo(reservation.id());
        assertThat(cancelled.isActive()).isFalse();
        assertThat(reservation.isActive()).as("original is unchanged").isTrue();
    }

    @Test
    void rejectsEndNotAfterStart() {
        assertThatThrownBy(() -> new Reservation(reservation.id(), SUV_1, DEC_1, DEC_1, ReservationStatus.ACTIVE))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
