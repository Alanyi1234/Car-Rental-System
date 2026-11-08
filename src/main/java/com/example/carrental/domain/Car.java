package com.example.carrental.domain;

import java.util.Objects;

/**
 * A single physical car in the fleet, e.g. "SUV-2".
 */
public record Car(String id, CarType type) {

    public Car {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(type, "type");
    }
}
