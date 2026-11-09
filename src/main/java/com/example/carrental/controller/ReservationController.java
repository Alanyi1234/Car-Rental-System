package com.example.carrental.controller;

import com.example.carrental.model.Reservation;
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

import java.net.URI;
import java.time.Duration;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final RentalService rentalService;

    public ReservationController(RentalService rentalService) {
        this.rentalService = rentalService;
    }

    /** Create a reservation. Returns 201 Created with a Location header. */
    @PostMapping
    public ResponseEntity<ReservationResponse> create(@Valid @RequestBody ReservationRequest request) {
        Reservation reservation = rentalService.reserve(request.carType(), request.start(), request.days());
        return ResponseEntity
                .created(URI.create("/reservations/" + reservation.id()))
                .body(toResponse(reservation));
    }

    /** Get a reservation by id. */
    @GetMapping("/{id}")
    public ReservationResponse get(@PathVariable UUID id) {
        return toResponse(rentalService.getReservation(id));
    }

    /** Change the car type, start and/or length of a reservation. */
    @PatchMapping("/{id}")
    public ReservationResponse update(@PathVariable UUID id, @Valid @RequestBody ReservationUpdateRequest request) {
        return toResponse(rentalService.modify(id, request.carType(), request.start(), request.days()));
    }

    /** Cancel a reservation. Returns 204 No Content. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable UUID id) {
        rentalService.cancel(id);
        return ResponseEntity.noContent().build();
    }

    /** Converts the domain Reservation into the API response. */
    private static ReservationResponse toResponse(Reservation r) {
        int days = (int) Duration.between(r.start(), r.end()).toDays();
        return new ReservationResponse(r.id(), r.car().id(), r.car().type(), r.start(), r.end(), days, r.status());
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

    /** 404 Not Found: no reservation with that id. */
    @ExceptionHandler(NoSuchElementException.class)  // thrown by RentalService.getReservation
    public ResponseEntity<String> handleNotFound(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    /** 409 Conflict: the request is valid but can't be done right now. */
    @ExceptionHandler(IllegalStateException.class)  // no car free, someone just booked it (DB constraint), or reservation is cancelled
    public ResponseEntity<String> handleConflict(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    /** 500 Internal Server Error: anything not handled above. */
    @ExceptionHandler(Exception.class)  // catch-all for unexpected errors
    public ResponseEntity<String> handleError(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Something went wrong: " + ex.getMessage());
    }
}
