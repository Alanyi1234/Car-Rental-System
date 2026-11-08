package com.example.carrental.controller;

import com.example.carrental.model.AvailabilityResponse;
import com.example.carrental.model.Car;
import com.example.carrental.model.CarType;
import com.example.carrental.service.RentalService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.List;

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

    /** The individual cars that are free for a period. With no type, returns free cars of every type. */
    @GetMapping("/availability/cars")
    public List<Car> availableCars(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam int days,
            @RequestParam(required = false) CarType type) {
        // TODO: call rentalService.availableCars(start, days, type)
        throw new UnsupportedOperationException("TODO: GET /availability/cars");
    }

    /** Bad input (missing or invalid fields, unreadable JSON, wrong parameter types) returns 400. */
    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<String> handleBadRequest(Exception ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Invalid request: " + ex.getMessage());
    }

    /** Generic fallback: any unhandled error returns 500 with a short message. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleError(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Something went wrong: " + ex.getMessage());
    }
}
