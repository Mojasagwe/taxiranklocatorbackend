-- Profiles seed for H2 tests (Flyway is off in tests). Keep in sync with V3__profiles_rank_accounts_and_applications.sql and V4__rider_profile_trips_and_settings.sql

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
INSERT INTO profile_permissions (profile_id, permission) SELECT id, 'TRIPS_OWN' FROM profiles WHERE code = 'COMMUTER';
