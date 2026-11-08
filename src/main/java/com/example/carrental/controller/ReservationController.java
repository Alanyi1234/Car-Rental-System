package com.example.carrental.controller;

import com.example.carrental.model.ReservationRequest;
import com.example.carrental.model.ReservationResponse;
import com.example.carrental.model.ReservationUpdateRequest;
import com.example.carrental.service.RentalService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
