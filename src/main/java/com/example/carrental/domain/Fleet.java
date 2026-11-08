package com.example.carrental.domain;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * The fixed set of cars available for rent, grouped by type.
 */
public final class Fleet {

    private final Map<CarType, List<Car>> carsByType = new EnumMap<>(CarType.class);

    private Fleet() {
    }

    /** Builds a fleet with the given number of cars per type (from fleet.capacity in application.yml). */
    public static Fleet withCapacities(Map<CarType, Integer> capacities) {
        // TODO: create the cars for each type. Returns an empty fleet for now so the app starts.
        return new Fleet();
    }

    public List<Car> carsOf(CarType type) {
        // TODO
        throw new UnsupportedOperationException("TODO: carsOf");
    }

    public int capacityOf(CarType type) {
        // TODO
        throw new UnsupportedOperationException("TODO: capacityOf");
    }
}
