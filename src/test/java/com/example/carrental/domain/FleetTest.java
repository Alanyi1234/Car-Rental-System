package com.example.carrental.domain;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FleetTest {

    @Test
    void createsTheConfiguredNumberOfCarsPerType() {
        Fleet fleet = Fleet.withCapacities(Map.of(CarType.SEDAN, 3, CarType.SUV, 2, CarType.VAN, 1));

        assertThat(fleet.capacityOf(CarType.SEDAN)).isEqualTo(3);
        assertThat(fleet.capacityOf(CarType.SUV)).isEqualTo(2);
        assertThat(fleet.capacityOf(CarType.VAN)).isEqualTo(1);
        assertThat(fleet.carsOf(CarType.SUV))
                .extracting(Car::id)
                .containsExactly("SUV-1", "SUV-2");
    }

    @Test
    void missingTypeHasZeroCars() {
        Fleet fleet = Fleet.withCapacities(Map.of(CarType.SEDAN, 1));

        assertThat(fleet.capacityOf(CarType.VAN)).isZero();
        assertThat(fleet.carsOf(CarType.VAN)).isEmpty();
    }

    @Test
    void rejectsNegativeCapacity() {
        assertThatThrownBy(() -> Fleet.withCapacities(Map.of(CarType.SUV, -1)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void carListsCannotBeModifiedFromOutside() {
        Fleet fleet = Fleet.withCapacities(Map.of(CarType.SUV, 1));

        assertThatThrownBy(() -> fleet.carsOf(CarType.SUV).add(new Car("SUV-99", CarType.SUV)))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
