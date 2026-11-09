package com.example.carrental.controller;

import com.example.carrental.model.Reservation;
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

import java.net.URI;
import java.time.Duration;
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
}
