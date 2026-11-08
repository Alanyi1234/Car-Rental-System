package com.example.carrental.controller;

import com.example.carrental.domain.Reservation;
import com.example.carrental.dto.ReservationRequest;
import com.example.carrental.dto.ReservationResponse;
import com.example.carrental.dto.ReservationUpdateRequest;
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
import java.util.UUID;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final RentalService rentalService;

    public ReservationController(RentalService rentalService) {
        this.rentalService = rentalService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> create(@Valid @RequestBody ReservationRequest request) {
        Reservation reservation = rentalService.reserve(request.carType(), request.start(), request.days());
        return ResponseEntity
                .created(URI.create("/reservations/" + reservation.id()))
                .body(ReservationResponse.from(reservation));
    }

    @GetMapping("/{id}")
    public ReservationResponse get(@PathVariable UUID id) {
        return ReservationResponse.from(rentalService.getReservation(id));
    }

    @PatchMapping("/{id}")
    public ReservationResponse update(@PathVariable UUID id, @Valid @RequestBody ReservationUpdateRequest request) {
        Reservation updated = rentalService.modify(id, request.carType(), request.start(), request.days());
        return ReservationResponse.from(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable UUID id) {
        rentalService.cancel(id);
        return ResponseEntity.noContent().build();
    }
}
