package com.example.carrental.controller;

import com.example.carrental.model.CarType;
import com.example.carrental.model.AvailabilityResponse;
import com.example.carrental.service.RentalService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

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
        // TODO: call rentalService.availability(start, days) and build the response
        throw new UnsupportedOperationException("TODO: GET /availability");
    }
}
