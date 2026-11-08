# Car Rental System

A simulated car rental system: reserve a car of a given type (sedan, SUV, van) at a desired
date and time for a given number of days, with a limited number of cars per type.

Written in Java 21 with Spring Boot 3 annotations.

> **Status: skeleton.** The packages, classes, endpoints and method signatures are in place.
> Method bodies are `TODO` stubs that throw `UnsupportedOperationException`, which the API
> returns as **501 Not Implemented** until each one is filled in. There is no build setup;
> the code is meant to be read, not run.

Fleet size is configured in `src/main/resources/application.yml`:

```yaml
fleet:
  capacity:
    SEDAN: 3
    SUV: 2
    VAN: 1
```

## API

| Method | Path | Purpose | Intended success |
|---|---|---|---|
| GET | `/availability?start=…&days=…[&carType=…]` | Free cars per type for a period | 200 |
| POST | `/reservations` | Reserve a car of a type | 201 + `Location` |
| GET | `/reservations/{id}` | View a reservation | 200 |
| PATCH | `/reservations/{id}` | Change type, start and/or days | 200 |
| DELETE | `/reservations/{id}` | Cancel (frees the car) | 204 |

Errors use RFC 9457 `ProblemDetail` bodies (see `ApiExceptionHandler`).

## Project layout

```
controller/   REST controllers, exception → HTTP mapping                     (Spring)
dto/          Request/response records for the REST API                      (plain Java)
config/       Wires the domain into Spring, binds fleet capacity             (Spring)
service/      RentalService: reserve, modify, cancel, availability           (plain Java)
domain/       CarType, Car, Reservation, Fleet, domain exceptions            (plain Java)
repository/   ReservationRepository + in-memory implementation               (plain Java)
```

`docs/schema.sql` sketches a possible database schema for a real persistence layer.
