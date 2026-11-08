package com.example.carrental.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * The fixed set of cars available for rent, grouped by type.
 *
 * <p>Capacity is not stored separately: it is simply the number of cars of a type,
 * so there is a single source of truth.
 */
public final class Fleet {

    private final Map<CarType, List<Car>> carsByType = new EnumMap<>(CarType.class);

    private Fleet(Map<CarType, List<Car>> carsByType) {
        this.carsByType.putAll(carsByType);
    }

    /**
     * Builds a fleet with the given number of cars per type. Types that are not
     * mentioned get zero cars. Car ids are generated as "SEDAN-1", "SEDAN-2", ...
     */
    public static Fleet withCapacities(Map<CarType, Integer> capacities) {
        Map<CarType, List<Car>> cars = new EnumMap<>(CarType.class);
        for (CarType type : CarType.values()) {
            int count = capacities.getOrDefault(type, 0);
            if (count < 0) {
                throw new IllegalArgumentException("Capacity for " + type + " must not be negative");
            }
            List<Car> list = new ArrayList<>(count);
            for (int i = 1; i <= count; i++) {
                list.add(new Car(type.name() + "-" + i, type));
            }
            cars.put(type, Collections.unmodifiableList(list));
        }
        return new Fleet(cars);
    }

    public List<Car> carsOf(CarType type) {
        return carsByType.get(type);
    }

    public int capacityOf(CarType type) {
        return carsOf(type).size();
    }
}
