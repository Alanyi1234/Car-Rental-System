# Car Rental System

A simulated car rental system: reserve a car of a given type (sedan, SUV, van) at a desired
date and time for a given number of days, with a limited number of cars per type.

Built with Java 21, Spring Boot 3 and Gradle. The booking logic is plain Java; Spring only
exposes it over HTTP.

## Running

```bash
./gradlew test        # run all tests
./gradlew bootRun     # start the API on http://localhost:8080
```

Fleet size is configured in `src/main/resources/application.yml`:

```yaml
fleet:
  capacity:
    SEDAN: 3
    SUV: 2
    VAN: 1
```

## API

| Method | Path | Purpose | Success | Errors |
|---|---|---|---|---|
| GET | `/availability?start=…&days=…[&carType=…]` | Free cars per type for a period | 200 | 400 |
| POST | `/reservations` | Reserve a car of a type | 201 + `Location` | 400, 409 |
| GET | `/reservations/{id}` | View a reservation | 200 | 404 |
| PATCH | `/reservations/{id}` | Change type, start and/or days | 200 | 400, 404, 409 |
| DELETE | `/reservations/{id}` | Cancel (frees the car) | 204 | 404 |

Errors use RFC 9457 `ProblemDetail` bodies.

```bash
# What's free?
curl "localhost:8080/availability?start=2030-12-01T10:00:00&days=3"

# Book an SUV
curl -i -X POST localhost:8080/reservations \
  -H 'Content-Type: application/json' \
  -d '{"carType":"SUV","start":"2030-12-01T10:00:00","days":3}'

# Extend it to 5 days
curl -X PATCH localhost:8080/reservations/{id} \
  -H 'Content-Type: application/json' -d '{"days":5}'

# Cancel
curl -X DELETE localhost:8080/reservations/{id}
```

## Design

```
api/          Controllers, request/response DTOs, exception → HTTP mapping   (Spring)
config/       Wires the domain into Spring, binds fleet capacity             (Spring)
service/      RentalService: reserve, modify, cancel, availability           (plain Java)
domain/       CarType, Car, Reservation, Fleet, domain exceptions            (plain Java)
repository/   ReservationRepository + in-memory implementation               (plain Java)
```

The domain and service have no Spring imports, so the core logic is tested with fast plain
unit tests, and the framework could be swapped without touching it.

### Key decisions

**Book a specific car, not a count.** Each reservation is assigned a concrete car (`SUV-2`).
A request succeeds if at least one car of the type has no overlapping active reservation.
This matches how rentals work and is simple to reason about. The alternative, tracking
only a count per type, must check that the number of *concurrent* bookings stays below capacity at every
instant (not just the number of bookings overlapping the request, since those may not overlap
each other). That approach is more permissive because it assumes cars can be reshuffled later.
The trade-off of concrete assignment is that it can fragment: a request may be rejected even
though moving existing bookings between cars would make room.

**Half-open periods `[start, end)`.** `end = start + days`. Two periods overlap iff
`a.start < b.end && b.start < a.end`, so a car returned at 10:00 can go out again at 10:00.

**The request takes `days`, as the requirement states.** Internally, it is converted to a start/end range
because overlap checks work on ranges. Supporting hourly rentals would only change the request DTO.

**Injected `Clock`.** "Start must not be in the past" uses an injected `Clock`, so tests
control "now". This check lives only in the service, so there is a single source of "now".

**Immutable `Reservation`.** Modifications create a new instance and are saved only on success,
so a rejected PATCH leaves the original reservation untouched.

**PATCH keeps the same car when possible**, otherwise switches to another free car of the
requested type. It ignores the reservation being modified when checking for overlaps, so
shifting a booking by a day doesn't conflict with itself.

**Cancel is a status change, and it is idempotent.** `DELETE` marks the reservation `CANCELLED`, which keeps it
for history and frees the car. Cancelling twice is a no-op. Modifying a cancelled reservation returns 409.

**Availability is a snapshot.** `GET /availability` drives the UI, but it holds nothing, so
another client can take the last car before the user's POST. `POST /reservations` is the only
guarantee, and clients must handle 409. Holding a car temporarily (like airline seat holds)
would be a separate feature.

**POST to create, PATCH to modify.** The server chooses the ID, the car and the end time, so
creation is a POST to the collection. PATCH sends only the changed fields. PUT, meaning "replace with exactly
this", doesn't fit: the client doesn't know the whole resource, and a delayed PUT retry could
overwrite a later change.

**Concurrency.** Every read-then-write operation in `RentalService` is `synchronized`, so
concurrent requests can never both take the last car (see `RentalServiceConcurrencyTest`).
A lock per car type would allow more parallelism. With several app servers, a JVM lock is no
longer enough. Correctness would then move to the database (below).

## Tests

| Test | Covers |
|---|---|
| `ReservationTest` | Overlap rules incl. every boundary case, immutability |
| `FleetTest` | Capacity per type, car ids, invalid capacity |
| `RentalServiceTest` | Reserve within/over capacity, partial overlaps, back-to-back, independent types, validation, cancel frees car, modify (extend, shift, switch car, change type, failure leaves original), availability |
| `RentalServiceConcurrencyTest` | 20 threads racing for 3 SUVs → exactly 3 bookings, no duplicate cars |
| `ReservationControllerTest` | HTTP mapping: status codes, validation, JSON shape (service mocked) |
| `CarRentalApplicationIT` | End to end through the real Spring context: book until full → 409 → cancel → book again |

## Production considerations (not implemented)

**Database.** PostgreSQL is a natural fit: the data is relational and correctness depends on
transactions. [`docs/schema.sql`](docs/schema.sql) sketches the schema. Its key feature is an
exclusion constraint that makes the database itself reject overlapping active bookings of the
same car, even if several app servers race:

```sql
EXCLUDE USING gist (car_id WITH =, tsrange(start_time, end_time) WITH &&) WHERE (status = 'ACTIVE')
```

The application check gives a clean 409 message, and the constraint guarantees correctness. A
JPA-backed `ReservationRepository` would replace the in-memory one without changing the service.

**Idempotency.** POST is not idempotent: if the response is lost and the client retries, it
could book twice. The fix is an `Idempotency-Key` header. The client generates it once per booking
attempt. The server stores the key with the response in the same transaction as the
reservation, and replays that response for retries (table sketched in `schema.sql`).

**Scaling.** App servers are stateless, so they can scale horizontally behind a load balancer.
The database constraint keeps bookings correct across servers. `GET /availability` is the most frequently called endpoint
and could be cached briefly or served from read replicas.

**Other extensions.**
- Customers and authentication.
- Optimistic locking on PATCH via `ETag`/`If-Match`.
- `OffsetDateTime`/`Instant` for multiple locations in different time zones.
- Lifecycle actions such as `POST /reservations/{id}/pickup` and `/return`, modeled as action sub-resources because each has its own rules.

## Assumptions

- Single rental location and time zone (`LocalDateTime`).
- A rental is a whole number of days, at least 1. Start may be any time that is not in the past.
- All cars of a type are interchangeable.
- No customer data, payment or authentication.
