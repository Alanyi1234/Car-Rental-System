package com.example.carrental.controller;

import com.example.carrental.model.Reservation;
import com.example.carrental.model.ReservationRequest;
import com.example.carrental.model.ReservationUpdateRequest;
import com.example.carrental.service.RentalService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final RentalService rentalService;

    public ReservationController(RentalService rentalService) {
        this.rentalService = rentalService;
    }

    /**
     * Create a reservation for the logged-in user. Returns 201 Created with a Location header.
     * The user id comes from the JWT's {@code sub} claim, not from the request body.
     */
    @PostMapping
    public ResponseEntity<Reservation> create(@Valid @RequestBody ReservationRequest request,
                                              @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        Reservation reservation = rentalService.reserve(userId, request.carType(), request.start(), request.days());
        return ResponseEntity
                .created(URI.create("/reservations/" + reservation.id()))
                .body(reservation);
    }

    /** Get a reservation by id. */
    @GetMapping("/{id}")
    public Reservation get(@PathVariable UUID id) {
        return rentalService.getReservation(id);
    }

    /** Change the car type, start and/or length of a reservation. */
    @PatchMapping("/{id}")
    public Reservation update(@PathVariable UUID id, @Valid @RequestBody ReservationUpdateRequest request) {
        return rentalService.modify(id, request.carType(), request.start(), request.days());
    }

    /** Cancel a reservation. Returns 204 No Content. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable UUID id) {
        rentalService.cancel(id);
        return ResponseEntity.noContent().build();
    }
}
