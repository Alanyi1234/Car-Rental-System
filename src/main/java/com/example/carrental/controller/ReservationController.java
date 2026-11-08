package com.example.carrental.controller;

import com.example.carrental.model.ReservationRequest;
import com.example.carrental.model.ReservationResponse;
import com.example.carrental.model.ReservationUpdateRequest;
import com.example.carrental.service.RentalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.UUID;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final RentalService rentalService;

    public ReservationController(RentalService rentalService) {
        this.rentalService = rentalService;
    }

    /** Create a reservation. Should return 201 Created with a Location header. */
    @PostMapping
    public ResponseEntity<ReservationResponse> create(@Valid @RequestBody ReservationRequest request) {
        // TODO: call rentalService.reserve(...) and return 201 Created
        throw new UnsupportedOperationException("TODO: POST /reservations");
    }

    /** Get a reservation by id. */
    @GetMapping("/{id}")
    public ReservationResponse get(@PathVariable UUID id) {
        // TODO: call rentalService.getReservation(id)
        throw new UnsupportedOperationException("TODO: GET /reservations/{id}");
    }

    /** Change the car type, start and/or length of a reservation. */
    @PatchMapping("/{id}")
    public ReservationResponse update(@PathVariable UUID id, @Valid @RequestBody ReservationUpdateRequest request) {
        // TODO: call rentalService.modify(...)
        throw new UnsupportedOperationException("TODO: PATCH /reservations/{id}");
    }

    /** Cancel a reservation. Should return 204 No Content. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable UUID id) {
        // TODO: call rentalService.cancel(id) and return 204 No Content
        throw new UnsupportedOperationException("TODO: DELETE /reservations/{id}");
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

    /** No car available, or someone else just booked it (DB constraint), returns 409. */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleConflict(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    /** Generic fallback: any unhandled error returns 500 with a short message. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleError(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Something went wrong: " + ex.getMessage());
    }
}
