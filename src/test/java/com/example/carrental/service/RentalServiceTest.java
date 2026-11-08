package com.example.carrental.service;

import com.example.carrental.domain.CarType;
import com.example.carrental.domain.Fleet;
import com.example.carrental.domain.NoAvailabilityException;
import com.example.carrental.domain.Reservation;
import com.example.carrental.domain.ReservationCancelledException;
import com.example.carrental.domain.ReservationNotFoundException;
import com.example.carrental.domain.ReservationStatus;
import com.example.carrental.repository.InMemoryReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RentalServiceTest {

    /** "Now" for every test, so results never depend on when the tests run. */
    private static final LocalDateTime NOW = LocalDateTime.of(2030, 1, 1, 9, 0);
    private static final LocalDateTime DEC_1 = LocalDateTime.of(2030, 12, 1, 10, 0);

    private RentalService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(NOW.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
        Fleet fleet = Fleet.withCapacities(Map.of(CarType.SEDAN, 2, CarType.SUV, 1, CarType.VAN, 1));
        service = new RentalService(fleet, new InMemoryReservationRepository(), clock);
    }

    @Nested
    class Reserve {

        @Test
        void reservesCarOfRequestedTypeForRequestedPeriod() {
            Reservation r = service.reserve(CarType.SUV, DEC_1, 3);

            assertThat(r.carType()).isEqualTo(CarType.SUV);
            assertThat(r.car().id()).isEqualTo("SUV-1");
            assertThat(r.start()).isEqualTo(DEC_1);
            assertThat(r.end()).isEqualTo(DEC_1.plusDays(3));
            assertThat(r.status()).isEqualTo(ReservationStatus.ACTIVE);
        }

        @Test
        void eachReservationGetsADifferentCarUntilTypeIsFull() {
            Reservation first = service.reserve(CarType.SEDAN, DEC_1, 3);
            Reservation second = service.reserve(CarType.SEDAN, DEC_1, 3);

            assertThat(first.car()).isNotEqualTo(second.car());
        }

        @Test
        void rejectsWhenAllCarsOfTypeAreBookedForOverlappingPeriod() {
            service.reserve(CarType.SEDAN, DEC_1, 3);
            service.reserve(CarType.SEDAN, DEC_1, 3);

            assertThatThrownBy(() -> service.reserve(CarType.SEDAN, DEC_1.plusDays(1), 1))
                    .isInstanceOf(NoAvailabilityException.class);
        }

        @Test
        void rejectsPartialOverlapAtStart() {
            service.reserve(CarType.SUV, DEC_1, 3);

            assertThatThrownBy(() -> service.reserve(CarType.SUV, DEC_1.minusDays(1), 2))
                    .isInstanceOf(NoAvailabilityException.class);
        }

        @Test
        void rejectsPartialOverlapAtEnd() {
            service.reserve(CarType.SUV, DEC_1, 3);

            assertThatThrownBy(() -> service.reserve(CarType.SUV, DEC_1.plusDays(2), 5))
                    .isInstanceOf(NoAvailabilityException.class);
        }

        @Test
        void rejectsPeriodThatContainsExistingBooking() {
            service.reserve(CarType.SUV, DEC_1, 1);

            assertThatThrownBy(() -> service.reserve(CarType.SUV, DEC_1.minusDays(5), 10))
                    .isInstanceOf(NoAvailabilityException.class);
        }

        @Test
        void allowsBackToBackBookingOfSameCar() {
            Reservation first = service.reserve(CarType.SUV, DEC_1, 3);
            Reservation second = service.reserve(CarType.SUV, first.end(), 2);

            assertThat(second.car()).isEqualTo(first.car());
            assertThat(second.start()).isEqualTo(first.end());
        }

        @Test
        void allowsBookingThatEndsExactlyWhenExistingOneStarts() {
            service.reserve(CarType.SUV, DEC_1, 3);

            Reservation earlier = service.reserve(CarType.SUV, DEC_1.minusDays(2), 2);

            assertThat(earlier.end()).isEqualTo(DEC_1);
        }

        @Test
        void allowsNonOverlappingBookingsWhenTypeIsFull() {
            service.reserve(CarType.SUV, DEC_1, 3);

            Reservation later = service.reserve(CarType.SUV, DEC_1.plusDays(10), 3);

            assertThat(later).isNotNull();
        }

        @Test
        void carTypesAreIndependent() {
            service.reserve(CarType.SUV, DEC_1, 3);

            assertThat(service.reserve(CarType.VAN, DEC_1, 3).carType()).isEqualTo(CarType.VAN);
            assertThat(service.reserve(CarType.SEDAN, DEC_1, 3).carType()).isEqualTo(CarType.SEDAN);
        }

        @Test
        void allowsStartingExactlyNow() {
            assertThat(service.reserve(CarType.SUV, NOW, 1).start()).isEqualTo(NOW);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, -10})
        void rejectsDaysLessThanOne(int days) {
            assertThatThrownBy(() -> service.reserve(CarType.SUV, DEC_1, days))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("days");
        }

        @Test
        void rejectsStartInThePast() {
            assertThatThrownBy(() -> service.reserve(CarType.SUV, NOW.minusMinutes(1), 1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("past");
        }

        @Test
        void rejectsNullCarType() {
            assertThatThrownBy(() -> service.reserve(null, DEC_1, 1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void rejectsNullStart() {
            assertThatThrownBy(() -> service.reserve(CarType.SUV, null, 1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void rejectsTypeWithNoCarsInFleet() {
            Clock clock = Clock.fixed(NOW.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
            RentalService noVans = new RentalService(
                    Fleet.withCapacities(Map.of(CarType.SEDAN, 1)), new InMemoryReservationRepository(), clock);

            assertThatThrownBy(() -> noVans.reserve(CarType.VAN, DEC_1, 1))
                    .isInstanceOf(NoAvailabilityException.class);
        }
    }

    @Nested
    class GetReservation {

        @Test
        void returnsSavedReservation() {
            Reservation r = service.reserve(CarType.SUV, DEC_1, 3);

            assertThat(service.getReservation(r.id())).isEqualTo(r);
        }

        @Test
        void throwsWhenNotFound() {
            assertThatThrownBy(() -> service.getReservation(UUID.randomUUID()))
                    .isInstanceOf(ReservationNotFoundException.class);
        }
    }

    @Nested
    class Cancel {

        @Test
        void cancellingFreesTheCar() {
            Reservation r = service.reserve(CarType.SUV, DEC_1, 3);

            service.cancel(r.id());

            assertThat(service.getReservation(r.id()).status()).isEqualTo(ReservationStatus.CANCELLED);
            assertThat(service.reserve(CarType.SUV, DEC_1, 3).car()).isEqualTo(r.car());
        }

        @Test
        void cancellingTwiceIsANoOp() {
            Reservation r = service.reserve(CarType.SUV, DEC_1, 3);

            service.cancel(r.id());
            service.cancel(r.id());

            assertThat(service.getReservation(r.id()).status()).isEqualTo(ReservationStatus.CANCELLED);
        }

        @Test
        void throwsWhenNotFound() {
            assertThatThrownBy(() -> service.cancel(UUID.randomUUID()))
                    .isInstanceOf(ReservationNotFoundException.class);
        }
    }

    @Nested
    class Modify {

        @Test
        void extendsReservationKeepingSameCar() {
            Reservation r = service.reserve(CarType.SUV, DEC_1, 3);

            Reservation updated = service.modify(r.id(), null, null, 5);

            assertThat(updated.id()).isEqualTo(r.id());
            assertThat(updated.car()).isEqualTo(r.car());
            assertThat(updated.end()).isEqualTo(DEC_1.plusDays(5));
        }

        @Test
        void doesNotConflictWithItself() {
            // Only one SUV: shifting by one day overlaps the reservation's own old period
            Reservation r = service.reserve(CarType.SUV, DEC_1, 3);

            Reservation updated = service.modify(r.id(), null, DEC_1.plusDays(1), null);

            assertThat(updated.start()).isEqualTo(DEC_1.plusDays(1));
            assertThat(updated.days()).isEqualTo(3);
        }

        @Test
        void switchesToAnotherCarOfSameTypeWhenCurrentOneIsTaken() {
            Reservation mine = service.reserve(CarType.SEDAN, DEC_1, 2);        // SEDAN-1
            service.reserve(CarType.SEDAN, DEC_1.plusDays(2), 3);               // SEDAN-1 again (back-to-back)

            Reservation extended = service.modify(mine.id(), null, null, 4);   // SEDAN-1 is busy on day 3-4

            assertThat(extended.car().id()).isEqualTo("SEDAN-2");
            assertThat(extended.days()).isEqualTo(4);
        }

        @Test
        void changesCarType() {
            Reservation r = service.reserve(CarType.SUV, DEC_1, 3);

            Reservation updated = service.modify(r.id(), CarType.VAN, null, null);

            assertThat(updated.carType()).isEqualTo(CarType.VAN);
            // the SUV is free again
            assertThat(service.reserve(CarType.SUV, DEC_1, 3)).isNotNull();
        }

        @Test
        void leavesOriginalUnchangedWhenNewPeriodIsUnavailable() {
            Reservation mine = service.reserve(CarType.SUV, DEC_1, 3);
            service.reserve(CarType.SUV, DEC_1.plusDays(3), 3);

            assertThatThrownBy(() -> service.modify(mine.id(), null, null, 5))
                    .isInstanceOf(NoAvailabilityException.class);
            assertThat(service.getReservation(mine.id())).isEqualTo(mine);
        }

        @Test
        void rejectsModifyingCancelledReservation() {
            Reservation r = service.reserve(CarType.SUV, DEC_1, 3);
            service.cancel(r.id());

            assertThatThrownBy(() -> service.modify(r.id(), null, null, 5))
                    .isInstanceOf(ReservationCancelledException.class);
        }

        @Test
        void rejectsNewStartInThePast() {
            Reservation r = service.reserve(CarType.SUV, DEC_1, 3);

            assertThatThrownBy(() -> service.modify(r.id(), null, NOW.minusDays(1), null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void rejectsInvalidDays() {
            Reservation r = service.reserve(CarType.SUV, DEC_1, 3);

            assertThatThrownBy(() -> service.modify(r.id(), null, null, 0))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    class Availability {

        @Test
        void reportsFullCapacityWhenNothingIsBooked() {
            assertThat(service.availability(DEC_1, 3)).containsExactlyInAnyOrderEntriesOf(
                    Map.of(CarType.SEDAN, 2, CarType.SUV, 1, CarType.VAN, 1));
        }

        @Test
        void countsOnlyCarsFreeForTheWholePeriod() {
            service.reserve(CarType.SEDAN, DEC_1.plusDays(2), 1);   // overlaps the queried period
            service.reserve(CarType.SUV, DEC_1.plusDays(3), 1);     // starts exactly when the period ends

            Map<CarType, Integer> available = service.availability(DEC_1, 3);

            assertThat(available.get(CarType.SEDAN)).isEqualTo(1);
            assertThat(available.get(CarType.SUV)).isEqualTo(1);
            assertThat(available.get(CarType.VAN)).isEqualTo(1);
        }

        @Test
        void agreesWithReserve() {
            service.reserve(CarType.SUV, DEC_1, 3);

            assertThat(service.availability(DEC_1, 1).get(CarType.SUV)).isZero();
            assertThatThrownBy(() -> service.reserve(CarType.SUV, DEC_1, 1))
                    .isInstanceOf(NoAvailabilityException.class);
        }

        @Test
        void cancelledReservationsDoNotCount() {
            Reservation r = service.reserve(CarType.SUV, DEC_1, 3);
            service.cancel(r.id());

            assertThat(service.availability(DEC_1, 3).get(CarType.SUV)).isEqualTo(1);
        }
    }
}
