-- Find a taxi: where each route (terminal) goes and how the rank runs it, riders' trip taps, and trip feedback

-- Where the route ends: a map point (from Google place search once enabled) and optionally another rank
ALTER TABLE terminals ADD COLUMN destination_latitude double precision;
ALTER TABLE terminals ADD COLUMN destination_longitude double precision;
ALTER TABLE terminals ADD COLUMN destination_place_id varchar(255);
ALTER TABLE terminals ADD COLUMN ends_at_rank_id bigint REFERENCES taxi_ranks (id) ON DELETE SET NULL;
ALTER TABLE terminals ADD CONSTRAINT terminals_destination_point
    CHECK ((destination_latitude IS NULL) = (destination_longitude IS NULL));

-- Set by the rank admin
ALTER TABLE terminals ADD COLUMN bay varchar(50);
ALTER TABLE terminals ADD COLUMN interval_minutes integer CHECK (interval_minutes BETWEEN 1 AND 240);
ALTER TABLE terminals ADD COLUMN paused_on date; -- "not running today" applies to that date only
ALTER TABLE terminals ADD COLUMN first_taxi time;
ALTER TABLE terminals ADD COLUMN last_taxi_weekday time;
ALTER TABLE terminals ADD COLUMN last_taxi_saturday time;
ALTER TABLE terminals ADD COLUMN last_taxi_sunday time;
-- Rider fare reports before this are considered answered
ALTER TABLE terminals ADD COLUMN fare_confirmed_at timestamp;

-- Riders' taps: in the taxi, taxi left, arrived. ACTIVE = in the taxi now.
ALTER TABLE trips ADD COLUMN boarded_at timestamp;
ALTER TABLE trips ADD COLUMN departed_at timestamp;
ALTER TABLE trips ADD COLUMN arrived_at timestamp;
ALTER TABLE trips DROP CONSTRAINT IF EXISTS trips_status_check;
ALTER TABLE trips ADD CONSTRAINT trips_status_check CHECK (status IN ('PLANNED', 'ACTIVE', 'TAKEN'));
CREATE UNIQUE INDEX ux_trips_one_active_per_user ON trips (user_id) WHERE status = 'ACTIVE';
CREATE INDEX idx_trips_terminal_departed ON trips (terminal_id, departed_at);

-- Optional answers after a trip
CREATE TABLE trip_feedback (
    id bigserial PRIMARY KEY,
    trip_id bigint NOT NULL UNIQUE REFERENCES trips (id) ON DELETE CASCADE,
    terminal_id bigint REFERENCES terminals (id) ON DELETE SET NULL,
    fare_paid numeric(8, 2) CHECK (fare_paid > 0),
    queue varchar(20) CHECK (queue IN ('TAXI_LOADING', 'WAITED', 'LONG_QUEUE')),
    rating varchar(10) CHECK (rating IN ('GOOD', 'OK', 'BAD')),
    created_at timestamp NOT NULL
);
CREATE INDEX idx_trip_feedback_terminal_created ON trip_feedback (terminal_id, created_at);
