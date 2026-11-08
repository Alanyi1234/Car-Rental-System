package com.example.carrental.model;


import java.time.LocalDateTime;
import java.util.List;

public record AvailabilityResponse(
        LocalDateTime start,
        LocalDateTime end,
        List<TypeAvailability> available
) {

    public record TypeAvailability(CarType carType, int count) {
    }
}
