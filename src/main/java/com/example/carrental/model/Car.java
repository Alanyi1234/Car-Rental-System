package com.example.carrental.model;

import java.util.UUID;

/**
 * A single physical car in the fleet.
 */
public record Car(UUID id, CarType type) {
}
