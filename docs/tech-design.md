# Car Rental System – Technical Design

**Status:** Draft (skeleton in place, logic not implemented)
**Last updated:** 2026-11-08

## 1. Overview

A simulated car rental system. Customers can see which cars are free for a period, reserve a
car of a given type for a start date/time and a number of days, change a reservation, and
cancel it. The number of cars of each type is limited.

## 2. Goals and future additions

**Goals**
- Model the rental domain with clear object-oriented layers.
- Expose the main actions as a REST API.
- Keep all data access behind a repository layer.

**Future additions**
- **Pricing:** a price per day for each car, so reservations and availability can show the
  total cost.
- **Locations:** multiple rental locations, so a customer can choose where to pick up and
  drop off the car (possibly different places).

## 3. Architecture

The system is split into four layers. Each layer only talks to the one below it.

```mermaid
flowchart TD
    Client([Client]) --> Controllers
    subgraph Controllers[Controller layer]
        RC[ReservationController]
        AC[AvailabilityController]
    end
    Controllers --> RS
    subgraph Services[Service layer]
        RS[RentalService]
    end
    RS --> CR
    RS --> RR
    subgraph Repositories[Repository layer]
        CR[CarRepository]
        RR[ReservationRepository]
    end
    Repositories --> Store[(Data store - TBD)]
```

| Layer | Responsibility |
|---|---|
| **Controller** | Receives HTTP requests, validates input, returns responses. No business rules. Each controller has `@ExceptionHandler`s: bad input returns 400, and any other unhandled error returns 500 with a short message. `ReservationController` also returns 404 for an unknown reservation id, and 409 when no car is available or someone else just booked it. |
| **Service** | Business logic. `RentalService` handles the booking rules and calls the repositories for data. |
| **Repository** | The only place that reads or writes data. |
| **Model** | Plain data types shared by all layers. |

## 4. API

| # | Method | Path | Purpose |
|---|---|---|---|
| 4.1 | GET | `/availability` | Free cars per type for a period |
| 4.2 | GET | `/availability/cars` | The individual free cars for a period |
| 4.3 | POST | `/reservations` | Reserve a car of a type |
| 4.4 | GET | `/reservations/{id}` | View a reservation |
| 4.5 | PATCH | `/reservations/{id}` | Change type, start and/or days |
| 4.6 | DELETE | `/reservations/{id}` | Cancel a reservation |

Dates and times use ISO-8601 format, e.g. `2030-12-01T10:00:00`. The error codes below are
the intended ones.

### 4.1 Check availability – `GET /availability`

Shows how many cars of each type are free for the whole period. Used to show options
before booking. It is a snapshot only: it does not hold a car.

**Handled by:** `AvailabilityController.availability` → `RentalService.availability`

**Query parameters**

| Name | Type | Required | Description |
|---|---|---|---|
| `start` | date-time | Yes | Start of the rental, not in the past |
| `days` | integer | Yes | Number of days, at least 1 |
| `carType` | `SEDAN` / `SUV` / `VAN` | No | Only show this type |

**Example**

```http
GET /availability?start=2030-12-01T10:00:00&days=3
```

```http
HTTP/1.1 200 OK

{
  "start": "2030-12-01T10:00:00",
  "end": "2030-12-04T10:00:00",
  "available": [
    { "carType": "SEDAN", "count": 3 },
    { "carType": "SUV", "count": 1 },
    { "carType": "VAN", "count": 0 }
  ]
}
```

**Responses**

| Status | When |
|---|---|
| 200 OK | Availability returned |
| 400 Bad Request | Missing or invalid `start`/`days`, start in the past, unknown `carType` |

### 4.2 List available cars – `GET /availability/cars`

Lists the individual cars that are free for the whole period. Where 4.1 answers "how many
of each type are free?", this answers "which exact cars are free?". With a large fleet,
this list could be paginated (e.g. `page` and `size` parameters).

**Handled by:** `AvailabilityController.availableCars` → `RentalService.availableCars`

**Query parameters**

| Name | Type | Required | Description |
|---|---|---|---|
| `start` | date-time | Yes | Start of the rental, not in the past |
| `days` | integer | Yes | Number of days, at least 1 |
| `carType` | `SEDAN` / `SUV` / `VAN` | No | Only show cars of this type. If omitted, returns the free cars of every type. |

**Example**

```http
GET /availability/cars?start=2030-12-01T10:00:00&days=3&carType=SUV
```

```http
HTTP/1.1 200 OK

[
  { "id": "8d1e4b2a-7c3f-4a9e-b5d6-2f1a0c9e8b7d", "type": "SUV" },
  { "id": "c41f9a07-2e6b-4d8c-a3f5-9b0e1d7c6a28", "type": "SUV" }
]
```

**Responses**

| Status | When |
|---|---|
| 200 OK | List returned (empty if nothing is free) |
| 400 Bad Request | Missing or invalid `start`/`days`, start in the past, unknown `carType` |

### 4.3 Create a reservation – `POST /reservations`

Reserves one car of the requested type. The system picks a specific car that is free for
the whole period.

**Handled by:** `ReservationController.create` → `RentalService.reserve`

**Request body** (`ReservationRequest`)

| Field | Type | Required | Description |
|---|---|---|---|
| `carType` | `SEDAN` / `SUV` / `VAN` | Yes | Type of car wanted |
| `start` | date-time | Yes | Start of the rental, not in the past |
| `days` | integer | Yes | Number of days, at least 1 |

**Example**

```http
POST /reservations
Content-Type: application/json

{ "carType": "SUV", "start": "2030-12-01T10:00:00", "days": 3 }
```

```http
HTTP/1.1 201 Created
Location: /reservations/3f2a9c1e-5b7d-4e2a-9f4c-1a2b3c4d5e6f

{
  "id": "3f2a9c1e-5b7d-4e2a-9f4c-1a2b3c4d5e6f",
  "carId": "8d1e4b2a-7c3f-4a9e-b5d6-2f1a0c9e8b7d",
  "carType": "SUV",
  "start": "2030-12-01T10:00:00",
  "end": "2030-12-04T10:00:00",
  "days": 3,
  "status": "ACTIVE"
}
```

**Responses**

| Status | When |
|---|---|
| 201 Created | Reservation made; `Location` header points to it |
| 400 Bad Request | Missing or invalid fields, start in the past |
| 409 Conflict | No car of that type is free for the period, or someone else booked it at the same moment |

### 4.4 Get a reservation – `GET /reservations/{id}`

Returns one reservation, including cancelled ones.

**Handled by:** `ReservationController.get` → `RentalService.getReservation`

**Path parameters**

| Name | Type | Description |
|---|---|---|
| `id` | UUID | Reservation id |

**Example**

```http
GET /reservations/3f2a9c1e-5b7d-4e2a-9f4c-1a2b3c4d5e6f
```

```http
HTTP/1.1 200 OK

{
  "id": "3f2a9c1e-5b7d-4e2a-9f4c-1a2b3c4d5e6f",
  "carId": "8d1e4b2a-7c3f-4a9e-b5d6-2f1a0c9e8b7d",
  "carType": "SUV",
  "start": "2030-12-01T10:00:00",
  "end": "2030-12-04T10:00:00",
  "days": 3,
  "status": "ACTIVE"
}
```

**Responses**

| Status | When |
|---|---|
| 200 OK | Reservation returned |
| 404 Not Found | No reservation with that id |

### 4.5 Modify a reservation – `PATCH /reservations/{id}`

Changes the car type, start and/or number of days. Only the fields sent are changed. If the
change can't be made, the original reservation stays as it was.

**Handled by:** `ReservationController.update` → `RentalService.modify`

**Path parameters**

| Name | Type | Description |
|---|---|---|
| `id` | UUID | Reservation id |

**Request body** (`ReservationUpdateRequest`)

| Field | Type | Required | Description |
|---|---|---|---|
| `carType` | `SEDAN` / `SUV` / `VAN` | No | New car type |
| `start` | date-time | No | New start, not in the past |
| `days` | integer | No | New number of days, at least 1 |

**Example** – extend to 5 days

```http
PATCH /reservations/3f2a9c1e-5b7d-4e2a-9f4c-1a2b3c4d5e6f
Content-Type: application/json

{ "days": 5 }
```

```http
HTTP/1.1 200 OK

{
  "id": "3f2a9c1e-5b7d-4e2a-9f4c-1a2b3c4d5e6f",
  "carId": "8d1e4b2a-7c3f-4a9e-b5d6-2f1a0c9e8b7d",
  "carType": "SUV",
  "start": "2030-12-01T10:00:00",
  "end": "2030-12-06T10:00:00",
  "days": 5,
  "status": "ACTIVE"
}
```

**Responses**

| Status | When |
|---|---|
| 200 OK | Reservation updated |
| 400 Bad Request | Invalid fields, new start in the past |
| 404 Not Found | No reservation with that id |
| 409 Conflict | No car free for the new period, or the reservation is cancelled |

### 4.6 Cancel a reservation – `DELETE /reservations/{id}`

Cancels a reservation and frees its car. The reservation is kept with status `CANCELLED`
rather than deleted. Cancelling twice has no extra effect.

**Handled by:** `ReservationController.cancel` → `RentalService.cancel`

**Path parameters**

| Name | Type | Description |
|---|---|---|
| `id` | UUID | Reservation id |

**Example**

```http
DELETE /reservations/3f2a9c1e-5b7d-4e2a-9f4c-1a2b3c4d5e6f
```

```http
HTTP/1.1 204 No Content
```

**Responses**

| Status | When |
|---|---|
| 204 No Content | Reservation cancelled (or was already cancelled) |
| 404 Not Found | No reservation with that id |

## 5. Components

### 5.1 Controllers

| Controller | Purpose |
|---|---|
| `ReservationController` | Create, view, change and cancel reservations |
| `AvailabilityController` | Show how many cars of each type are free for a period |

### 5.2 Services

| Service | Methods |
|---|---|
| `RentalService` | `reserve`, `getReservation`, `modify`, `cancel`, `availability`, `availableCars` |

### 5.3 Repositories

| Repository | Methods |
|---|---|
| `CarRepository` | `findAll`, `findById`, `findByType`, `save`, `deleteById` |
| `ReservationRepository` | `findAll`, `findById`, `findActiveByCarId`, `save`, `deleteById` |

## 6. Data model

```mermaid
classDiagram
    class Car {
        UUID id
        CarType type
    }
    class Reservation {
        UUID id
        Car car
        LocalDateTime start
        LocalDateTime end
        ReservationStatus status
    }
    class CarType {
        <<enumeration>>
        SEDAN
        SUV
        VAN
    }
    class ReservationStatus {
        <<enumeration>>
        ACTIVE
        CANCELLED
    }
    Reservation --> Car
    Car --> CarType
    Reservation --> ReservationStatus
```

- A **Car** is one physical car of a given type, identified by a UUID.
- A **Reservation** books one car from `start` to `end`. Cancelling changes its status
  rather than deleting it.

### Database tables

Each repository reads and writes one table.

| Repository | Table | Columns |
|---|---|---|
| `CarRepository` | `cars` | `id` (PK), `type` |
| `ReservationRepository` | `reservations` | `id` (PK), `car_id` (FK → `cars.id`), `start_time`, `end_time`, `status` |

```mermaid
erDiagram
    cars ||--o{ reservations : "booked by"
    cars {
        uuid id PK
        string type
    }
    reservations {
        uuid id PK
        uuid car_id FK
        datetime start_time
        datetime end_time
        string status
    }
```

A car can have many reservations over time; each reservation is for exactly one car.

## 7. Main flow: reserving a car

1. Client sends `POST /reservations` with car type, start and number of days.
2. `ReservationController` validates the request and calls `RentalService.reserve`.
3. `RentalService` gets the cars of that type from `CarRepository`, and their active
   reservations from `ReservationRepository`.
4. It picks a car with no overlapping reservation. If none is free, the request is rejected.
5. It saves the new reservation through `ReservationRepository` and returns it. If the
   database rejects the save because someone just booked that car (section 8), the client
   gets a 409 Conflict.

### Repository calls per service method

| `RentalService` method | Repository calls |
|---|---|
| `reserve` | `CarRepository.findByType`, `ReservationRepository.findActiveByCarId`, `ReservationRepository.save` |
| `getReservation` | `ReservationRepository.findById` |
| `modify` | `ReservationRepository.findById`, `CarRepository.findByType`, `ReservationRepository.findActiveByCarId`, `ReservationRepository.save` |
| `cancel` | `ReservationRepository.findById`, `ReservationRepository.save` |
| `availability` | `CarRepository.findByType`, `ReservationRepository.findActiveByCarId` |
| `availableCars` | `CarRepository.findByType` (or `findAll` with no type), `ReservationRepository.findActiveByCarId` |

## 8. Concurrency

**Problem:** two people try to book the last free car of a type at the same moment. Both
requests check, both see the car as free, and both save a booking.

**Approach: a database constraint, no locks.** The `reservations` table has an exclusion
constraint that rejects any active booking overlapping another active booking of the same car:

```sql
CREATE EXTENSION IF NOT EXISTS btree_gist;

ALTER TABLE reservations
    ADD CONSTRAINT no_overlapping_bookings
    EXCLUDE USING gist (
        car_id WITH =,
        tsrange(start_time, end_time) WITH &&
    )
    WHERE (status = 'ACTIVE');
```

- **`car_id WITH =`**: same car.
- **`tsrange(start_time, end_time) WITH &&`**: overlapping time ranges. Ranges include the start
  but not the end, so back-to-back bookings are allowed.
- **`WHERE (status = 'ACTIVE')`**: cancelled bookings don't count.

**How the service uses it:** `RentalService.reserve` picks the first car that looks free and
saves a booking for it. If two requests race for the same car, the database accepts one save
and rejects the other (`DataIntegrityViolationException`). The losing request gets a
**409 Conflict** and the client can simply try again. `modify` works the same way.

**Why this approach:**
- Nothing is locked, so requests never wait for each other, even for popular car types.
- The guarantee lives in the database, so it holds across any number of app servers and
  even if the application logic has a bug.
- Conflicts on the exact same car at the exact same moment are rare in car rental, so 409s
  from this are rare too.
