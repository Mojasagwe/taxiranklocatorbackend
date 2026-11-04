-- Rider profile area: session revocation, notification preferences, account deletion and trip history

-- Bumped on password change, reset, deletion: tokens carrying an older version stop working
ALTER TABLE users ADD COLUMN token_version integer NOT NULL DEFAULT 0;

-- Notification preferences (saved now; push delivery comes with AWS)
ALTER TABLE users ADD COLUMN notify_application_updates boolean NOT NULL DEFAULT true;
ALTER TABLE users ADD COLUMN notify_rank_changes boolean NOT NULL DEFAULT true;
ALTER TABLE users ADD COLUMN notify_news boolean NOT NULL DEFAULT false;

-- DELETED: the person deleted their account; personal data is erased, the row stays for history
ALTER TABLE users ADD COLUMN deleted_at timestamp;
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_account_status_check;
ALTER TABLE users ADD CONSTRAINT users_account_status_check
    CHECK (account_status IN ('ACTIVE', 'FLAGGED', 'SUSPENDED', 'BLOCKED', 'DELETED'));

-- A trip planned or taken from a rank to one of its destinations (terminals).
-- user_id is cleared when the rider deletes their account, leaving only anonymous counts.
CREATE TABLE trips (
    id bigserial PRIMARY KEY,
    user_id bigint REFERENCES users (id) ON DELETE SET NULL,
    rank_id bigint NOT NULL REFERENCES taxi_ranks (id) ON DELETE CASCADE,
    terminal_id bigint REFERENCES terminals (id) ON DELETE SET NULL,
    destination varchar(255) NOT NULL,
    fare numeric(8, 2) NOT NULL,
    status varchar(20) NOT NULL CHECK (status IN ('PLANNED', 'TAKEN')),
    trip_date timestamp NOT NULL,
    created_at timestamp NOT NULL
);
CREATE INDEX idx_trips_user_date ON trips (user_id, trip_date DESC);
CREATE INDEX idx_trips_rank ON trips (rank_id);

INSERT INTO profile_permissions (profile_id, permission)
SELECT id, 'TRIPS_OWN' FROM profiles WHERE code = 'COMMUTER'
ON CONFLICT DO NOTHING;
