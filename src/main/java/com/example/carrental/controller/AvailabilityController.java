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

    /** 400 Bad Request: the client sent invalid input. */
    @ExceptionHandler({
            MethodArgumentNotValidException.class,          // body field fails validation, e.g. missing carType or days < 1
            HttpMessageNotReadableException.class,          // body isn't valid JSON, or has an unknown value like "TRUCK"
            MissingServletRequestParameterException.class,  // required query param missing, e.g. no start
            MethodArgumentTypeMismatchException.class,      // param has the wrong type, e.g. id isn't a UUID
            IllegalArgumentException.class                  // service rule broken: start in the past or days < 1
    })
    public ResponseEntity<String> handleBadRequest(Exception ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Invalid request: " + ex.getMessage());
    }

    /** 500 Internal Server Error: anything not handled above. */
    @ExceptionHandler(Exception.class)  // catch-all for unexpected errors
    public ResponseEntity<String> handleError(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Something went wrong: " + ex.getMessage());
    }
}
