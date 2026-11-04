-- Profiles and permissions replace the hardcoded users.role column.
-- Each rank gets at most one shared Rank Admin account (users.rank_id).
-- Rank applications replace admin registration requests and rank assignment requests.
-- Starts fresh: existing users and requests are removed (no production data exists, 2026-11-01).

DROP TABLE IF EXISTS rank_assignment_requests;
DROP TABLE IF EXISTS admin_request_ranks;
DROP TABLE IF EXISTS admin_registration_requests;
DROP TABLE IF EXISTS rank_admins;
DELETE FROM users;

CREATE TABLE profiles (
    id bigserial PRIMARY KEY,
    code varchar(50) NOT NULL UNIQUE,
    name varchar(100) NOT NULL,
    description varchar(255)
);

CREATE TABLE profile_permissions (
    profile_id bigint NOT NULL REFERENCES profiles (id),
    permission varchar(50) NOT NULL,
    PRIMARY KEY (profile_id, permission)
);

ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check;
ALTER TABLE users DROP COLUMN role;
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_account_status_check;
ALTER TABLE users ADD CONSTRAINT users_account_status_check
    CHECK (account_status IN ('ACTIVE', 'FLAGGED', 'SUSPENDED', 'BLOCKED'));

-- Rank accounts have no personal email, phone or surname
ALTER TABLE users ALTER COLUMN email DROP NOT NULL;
ALTER TABLE users ALTER COLUMN phone_number DROP NOT NULL;
ALTER TABLE users ALTER COLUMN last_name DROP NOT NULL;

ALTER TABLE users ADD COLUMN username varchar(50);
ALTER TABLE users ADD CONSTRAINT uk_users_username UNIQUE (username);
ALTER TABLE users ADD COLUMN profile_id bigint NOT NULL REFERENCES profiles (id);
ALTER TABLE users ADD COLUMN must_change_password boolean NOT NULL DEFAULT false;
ALTER TABLE users ADD COLUMN status_reason varchar(500);
ALTER TABLE users ADD COLUMN rank_id bigint REFERENCES taxi_ranks (id);
ALTER TABLE users ADD CONSTRAINT uk_users_rank UNIQUE (rank_id);
ALTER TABLE users ADD COLUMN responsible_person_name varchar(200);
ALTER TABLE users ADD COLUMN responsible_person_phone varchar(30);

CREATE TABLE account_events (
    id bigserial PRIMARY KEY,
    user_id bigint NOT NULL REFERENCES users (id),
    actor_id bigint REFERENCES users (id),
    action varchar(50) NOT NULL,
    details varchar(500),
    created_at timestamp(6) without time zone NOT NULL
);
CREATE INDEX idx_account_events_user ON account_events (user_id);

CREATE TABLE rank_applications (
    id bigserial PRIMARY KEY,
    applicant_id bigint NOT NULL REFERENCES users (id),
    rank_id bigint REFERENCES taxi_ranks (id),
    proposed_name varchar(100),
    proposed_address varchar(200),
    proposed_city varchar(100),
    proposed_province varchar(100),
    proposed_latitude double precision,
    proposed_longitude double precision,
    proposed_operating_hours varchar(50),
    proposed_capacity integer,
    proposed_contact_phone varchar(30),
    proposed_contact_email varchar(255),
    designation varchar(100) NOT NULL,
    motivation varchar(1000),
    status varchar(20) NOT NULL,
    reviewer_note varchar(1000),
    reviewed_by bigint REFERENCES users (id),
    reviewed_at timestamp(6) without time zone,
    rank_account_id bigint REFERENCES users (id),
    created_at timestamp(6) without time zone NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    CONSTRAINT rank_applications_target CHECK (rank_id IS NOT NULL OR proposed_name IS NOT NULL)
);
CREATE INDEX idx_rank_applications_status ON rank_applications (status);
CREATE INDEX idx_rank_applications_applicant ON rank_applications (applicant_id);

INSERT INTO profiles (code, name, description) VALUES
    ('SYSTEM_ADMIN', 'System Administrator', 'Manages users, rank accounts, applications and all ranks'),
    ('RANK_ADMIN', 'Rank Admin', 'Shared account for one taxi rank; manages that rank and its terminals'),
    ('COMMUTER', 'Commuter', 'Finds ranks and routes; can apply to manage a rank');

INSERT INTO profile_permissions (profile_id, permission) SELECT id, 'USERS_VIEW' FROM profiles WHERE code = 'SYSTEM_ADMIN';
INSERT INTO profile_permissions (profile_id, permission) SELECT id, 'USERS_MANAGE' FROM profiles WHERE code = 'SYSTEM_ADMIN';
INSERT INTO profile_permissions (profile_id, permission) SELECT id, 'USERS_RESET_PASSWORD' FROM profiles WHERE code = 'SYSTEM_ADMIN';
INSERT INTO profile_permissions (profile_id, permission) SELECT id, 'USERS_SET_STATUS' FROM profiles WHERE code = 'SYSTEM_ADMIN';
INSERT INTO profile_permissions (profile_id, permission) SELECT id, 'APPLICATIONS_REVIEW' FROM profiles WHERE code = 'SYSTEM_ADMIN';
INSERT INTO profile_permissions (profile_id, permission) SELECT id, 'RANKS_MANAGE' FROM profiles WHERE code = 'SYSTEM_ADMIN';
INSERT INTO profile_permissions (profile_id, permission) SELECT id, 'ROUTES_MANAGE' FROM profiles WHERE code = 'SYSTEM_ADMIN';
INSERT INTO profile_permissions (profile_id, permission) SELECT id, 'RANK_MANAGE_OWN' FROM profiles WHERE code = 'RANK_ADMIN';
INSERT INTO profile_permissions (profile_id, permission) SELECT id, 'APPLICATIONS_SUBMIT' FROM profiles WHERE code = 'COMMUTER';
