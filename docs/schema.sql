-- ============================================================================
-- Production schema (PostgreSQL) — NOT used by the exercise, which stores data
-- in memory behind ReservationRepository. Included to show how persistence
-- would work and how the database itself would prevent double booking.
-- ============================================================================

-- Needed so the exclusion constraint can combine "=" on car_id with "&&" on a range
CREATE EXTENSION IF NOT EXISTS btree_gist;

-- Lookup table instead of a hardcoded CHECK: adding a type is an INSERT, not a migration
CREATE TABLE car_type (
    code        VARCHAR(20) PRIMARY KEY            -- 'SEDAN', 'SUV', 'VAN'
);

-- Capacity per type = number of active rows here (no separate capacity column to drift)
CREATE TABLE car (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    plate       VARCHAR(20) NOT NULL UNIQUE,
    car_type    VARCHAR(20) NOT NULL REFERENCES car_type (code),
    active      BOOLEAN     NOT NULL DEFAULT TRUE  -- retire a car without losing history
);

CREATE INDEX idx_car_type ON car (car_type) WHERE active;

CREATE TABLE reservation (
    id          UUID        PRIMARY KEY,
    car_id      BIGINT      NOT NULL REFERENCES car (id),
    start_time  TIMESTAMP   NOT NULL,           -- single location, so no time zone (see README)
    end_time    TIMESTAMP   NOT NULL,
    status      VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
                CHECK (status IN ('ACTIVE', 'CANCELLED')),
    created_at  TIMESTAMP   NOT NULL DEFAULT now(),
    version     INT         NOT NULL DEFAULT 0, -- optimistic locking for PATCH

    CONSTRAINT valid_period CHECK (end_time > start_time),

    -- The database refuses overlapping ACTIVE bookings of the same car, even if two
    -- app servers race. tsrange defaults to '[)', so back-to-back bookings are allowed.
    CONSTRAINT no_double_booking EXCLUDE USING gist (
        car_id WITH =,
        tsrange(start_time, end_time) WITH &&
    ) WHERE (status = 'ACTIVE')
);

-- Idempotency for POST /reservations (not implemented in the exercise; see README).
-- A retry with the same key replays the stored response instead of booking again.
CREATE TABLE idempotency_key (
    key              VARCHAR(64) PRIMARY KEY,
    request_hash     CHAR(64)    NOT NULL,          -- SHA-256 of the body: same key + different body -> 422
    reservation_id   UUID        REFERENCES reservation (id),
    response_status  INT,                            -- NULL while the first request is in progress
    response_body    JSONB,
    created_at       TIMESTAMP   NOT NULL DEFAULT now(),
    expires_at       TIMESTAMP   NOT NULL DEFAULT now() + INTERVAL '24 hours'
);

CREATE INDEX idx_idempotency_expires ON idempotency_key (expires_at);

-- ----------------------------------------------------------------------------
-- Key queries
-- ----------------------------------------------------------------------------

-- Availability per type for [:start, :end). LEFT JOIN keeps sold-out types at 0.
-- SELECT t.code AS car_type, COUNT(c.id) AS available
-- FROM car_type t
-- LEFT JOIN car c
--        ON c.car_type = t.code
--       AND c.active
--       AND NOT EXISTS (
--             SELECT 1 FROM reservation r
--             WHERE r.car_id = c.id
--               AND r.status = 'ACTIVE'
--               AND r.start_time < :end
--               AND :start < r.end_time)
-- GROUP BY t.code;

-- Booking: pick a free car and lock it, then insert. If two requests still race,
-- no_double_booking rejects the second INSERT and the API returns 409.
-- SELECT c.id FROM car c
-- WHERE c.car_type = :type AND c.active
--   AND NOT EXISTS (/* same overlap check as above */)
-- LIMIT 1
-- FOR UPDATE SKIP LOCKED;
-- INSERT INTO reservation (id, car_id, start_time, end_time) VALUES (...);
