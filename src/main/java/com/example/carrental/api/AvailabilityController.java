package com.example.carrental.api;

import com.example.carrental.domain.CarType;
import com.example.carrental.service.RentalService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Read-only view used by a UI to show options before booking. The result is a snapshot;
 * POST /reservations is the only operation that guarantees a car.
 */
@RestController
public class AvailabilityController {

    private final RentalService rentalService;

    public AvailabilityController(RentalService rentalService) {
        this.rentalService = rentalService;
    }

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
}
