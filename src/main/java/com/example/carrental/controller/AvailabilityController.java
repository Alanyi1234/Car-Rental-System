package com.example.carrental.controller;

import com.example.carrental.model.AvailabilityResponse;
import com.example.carrental.model.Car;
import com.example.carrental.model.CarType;
import com.example.carrental.service.RentalService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
public class AvailabilityController {

    private final RentalService rentalService;

    public AvailabilityController(RentalService rentalService) {
        this.rentalService = rentalService;
    }

    /** Number of free cars per type for a period, optionally filtered to one type. */
    @GetMapping("/availability")
    public AvailabilityResponse availability(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam int days,
            @RequestParam(required = false) CarType carType) {
        Map<CarType, Integer> counts = rentalService.availability(start, days);

        List<AvailabilityResponse.TypeAvailability> available = counts.entrySet().stream()
                .filter(e -> carType == null || e.getKey() == carType)
                .map(e -> new AvailabilityResponse.TypeAvailability(e.getKey(), e.getValue()))
                .toList();

        return new AvailabilityResponse(start, start.plusDays(days), available);
    }

    /** The individual cars that are free for a period. With no type, returns free cars of every type. */
    @GetMapping("/availability/cars")
    public List<Car> availableCars(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam int days,
            @RequestParam(required = false) CarType carType) {
        return rentalService.availableCars(start, days, carType);
    }
}
