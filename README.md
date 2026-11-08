# Car Rental System

A simulated car rental system: reserve a car of a given type (sedan, SUV, van) at a desired
date and time for a given number of days, with a limited number of cars per type.

> **Status: skeleton.** Controllers, service and repository are in place with their method
> signatures. Method bodies are `TODO` stubs. There is no build setup; the code is meant to
> be read, not run.

## API

| Method | Path | Purpose |
|---|---|---|
| GET | `/availability?start=…&days=…[&carType=…]` | Free cars per type for a period |
| POST | `/reservations` | Reserve a car of a type |
| GET | `/reservations/{id}` | View a reservation |
| PATCH | `/reservations/{id}` | Change type, start and/or days |
| DELETE | `/reservations/{id}` | Cancel (frees the car) |

## Project layout

```
controller/   REST endpoints: ReservationController, AvailabilityController
service/      RentalService: reserve, getReservation, modify, cancel, availability
repository/   CarRepository, ReservationRepository (all data access goes through here)
model/        Data types: Car, CarType, Reservation, ReservationStatus, request/response records
```
