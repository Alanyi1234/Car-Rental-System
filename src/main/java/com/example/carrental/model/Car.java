package com.example.carrental.model;

/**
 * A single physical car in the fleet, e.g. "SUV-2".
 */
public record Car(String id, CarType type) {
}
