-- Baseline schema, generated from the Hibernate-managed database on 2026-11-01.
-- Existing databases are baselined at V1 (spring.flyway.baseline-on-migrate) and skip this file.

CREATE TABLE admin_registration_requests (
    id bigint NOT NULL,
    admin_notes character varying(500),
    created_at timestamp(6) without time zone,
    designation character varying(255),
    email character varying(255) NOT NULL,
    first_name character varying(255) NOT NULL,
    justification character varying(1000),
    last_name character varying(255) NOT NULL,
    password character varying(255) NOT NULL,
    phone_number character varying(255) NOT NULL,
    professional_experience character varying(1000),
    review_notes character varying(500),
    reviewed_at timestamp(6) without time zone,
    status character varying(255),
    updated_at timestamp(6) without time zone,
    reviewed_by bigint,
    CONSTRAINT admin_registration_requests_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'APPROVED'::character varying, 'REJECTED'::character varying, 'CANCELLED'::character varying])::text[])))
);

CREATE SEQUENCE admin_registration_requests_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE admin_registration_requests_id_seq OWNED BY admin_registration_requests.id;

CREATE TABLE admin_request_ranks (
    request_id bigint NOT NULL,
    rank_id bigint NOT NULL
);

CREATE TABLE rank_admins (
    id bigint NOT NULL,
    assigned_at timestamp(6) without time zone,
    can_edit_rank_details boolean NOT NULL,
    can_manage_drivers boolean NOT NULL,
    can_manage_routes boolean NOT NULL,
    can_manage_terminals boolean NOT NULL,
    can_view_financials boolean NOT NULL,
    designation character varying(255),
    last_updated timestamp(6) without time zone,
    notes character varying(500),
    rank_id bigint NOT NULL,
    user_id bigint NOT NULL
);

CREATE SEQUENCE rank_admins_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE rank_admins_id_seq OWNED BY rank_admins.id;

CREATE TABLE rank_assignment_requests (
    id bigint NOT NULL,
    request_date timestamp(6) without time zone NOT NULL,
    request_reason character varying(500),
    response_date timestamp(6) without time zone,
    response_message character varying(500),
    status character varying(255) NOT NULL,
    rank_id bigint NOT NULL,
    admin_id bigint NOT NULL,
    reviewer_id bigint,
    CONSTRAINT rank_assignment_requests_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'APPROVED'::character varying, 'REJECTED'::character varying, 'CANCELLED'::character varying])::text[])))
);

CREATE SEQUENCE rank_assignment_requests_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE rank_assignment_requests_id_seq OWNED BY rank_assignment_requests.id;

CREATE TABLE routes (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    distance double precision,
    estimated_duration numeric(21,0),
    fare numeric(38,2) NOT NULL,
    peak_hours character varying(255),
    traffic_status character varying(255),
    updated_at timestamp(6) without time zone,
    destination_rank_id bigint NOT NULL,
    source_rank_id bigint NOT NULL
);

CREATE SEQUENCE routes_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE routes_id_seq OWNED BY routes.id;

CREATE TABLE taxi_ranks (
    id bigint NOT NULL,
    address character varying(255) NOT NULL,
    capacity integer,
    city character varying(255),
    code character varying(255) NOT NULL,
    contact_email character varying(255),
    contact_phone character varying(255),
    created_at timestamp(6) without time zone,
    description character varying(255),
    is_active boolean,
    latitude double precision,
    longitude double precision,
    name character varying(255) NOT NULL,
    operating_hours character varying(255),
    province character varying(255),
    updated_at timestamp(6) without time zone
);

CREATE SEQUENCE taxi_ranks_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE taxi_ranks_id_seq OWNED BY taxi_ranks.id;

CREATE TABLE terminals (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    departure_schedule character varying(255),
    distance character varying(255) NOT NULL,
    fare double precision NOT NULL,
    is_active boolean,
    name character varying(255) NOT NULL,
    operating_days character varying(255),
    travel_time character varying(255) NOT NULL,
    updated_at timestamp(6) without time zone,
    taxi_rank_id bigint NOT NULL
);

CREATE SEQUENCE terminals_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE terminals_id_seq OWNED BY terminals.id;

CREATE TABLE users (
    id bigint NOT NULL,
    account_status character varying(255),
    created_at timestamp(6) without time zone,
    email character varying(255) NOT NULL,
    first_name character varying(255) NOT NULL,
    is_verified boolean,
    last_login timestamp(6) without time zone,
    last_name character varying(255) NOT NULL,
    password character varying(255) NOT NULL,
    phone_number character varying(255) NOT NULL,
    profile_picture character varying(255),
    rating numeric(2,1),
    role character varying(255),
    total_trips integer,
    updated_at timestamp(6) without time zone,
    CONSTRAINT users_account_status_check CHECK (((account_status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'INACTIVE'::character varying, 'SUSPENDED'::character varying, 'BLOCKED'::character varying])::text[]))),
    CONSTRAINT users_role_check CHECK (((role)::text = ANY ((ARRAY['RIDER'::character varying, 'ADMIN'::character varying, 'SUPER_ADMIN'::character varying])::text[])))
);

CREATE SEQUENCE users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE users_id_seq OWNED BY users.id;

ALTER TABLE ONLY admin_registration_requests ALTER COLUMN id SET DEFAULT nextval('admin_registration_requests_id_seq'::regclass);

ALTER TABLE ONLY rank_admins ALTER COLUMN id SET DEFAULT nextval('rank_admins_id_seq'::regclass);

ALTER TABLE ONLY rank_assignment_requests ALTER COLUMN id SET DEFAULT nextval('rank_assignment_requests_id_seq'::regclass);

ALTER TABLE ONLY routes ALTER COLUMN id SET DEFAULT nextval('routes_id_seq'::regclass);

ALTER TABLE ONLY taxi_ranks ALTER COLUMN id SET DEFAULT nextval('taxi_ranks_id_seq'::regclass);

ALTER TABLE ONLY terminals ALTER COLUMN id SET DEFAULT nextval('terminals_id_seq'::regclass);

ALTER TABLE ONLY users ALTER COLUMN id SET DEFAULT nextval('users_id_seq'::regclass);

ALTER TABLE ONLY admin_registration_requests
    ADD CONSTRAINT admin_registration_requests_pkey PRIMARY KEY (id);

ALTER TABLE ONLY admin_request_ranks
    ADD CONSTRAINT admin_request_ranks_pkey PRIMARY KEY (request_id, rank_id);

ALTER TABLE ONLY rank_admins
    ADD CONSTRAINT rank_admins_pkey PRIMARY KEY (id);

ALTER TABLE ONLY rank_assignment_requests
    ADD CONSTRAINT rank_assignment_requests_pkey PRIMARY KEY (id);

ALTER TABLE ONLY routes
    ADD CONSTRAINT routes_pkey PRIMARY KEY (id);

ALTER TABLE ONLY taxi_ranks
    ADD CONSTRAINT taxi_ranks_pkey PRIMARY KEY (id);

ALTER TABLE ONLY terminals
    ADD CONSTRAINT terminals_pkey PRIMARY KEY (id);

ALTER TABLE ONLY users
    ADD CONSTRAINT uk_6dotkott2kjsp8vw4d0m25fb7 UNIQUE (email);

ALTER TABLE ONLY users
    ADD CONSTRAINT uk_9q63snka3mdh91as4io72espi UNIQUE (phone_number);

ALTER TABLE ONLY taxi_ranks
    ADD CONSTRAINT uk_gijyst0mr4j24x4la9x1gmnrv UNIQUE (code);

ALTER TABLE ONLY admin_registration_requests
    ADD CONSTRAINT uk_ljne0uoregrbm0crf77u2k4rs UNIQUE (phone_number);

ALTER TABLE ONLY admin_registration_requests
    ADD CONSTRAINT uk_s62cgwlqlf7fmxjgyle2onjjy UNIQUE (email);

ALTER TABLE ONLY users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);

ALTER TABLE ONLY rank_assignment_requests
    ADD CONSTRAINT fk827ksvxbc4k9ntlry664d9wra FOREIGN KEY (reviewer_id) REFERENCES users(id);

ALTER TABLE ONLY routes
    ADD CONSTRAINT fk_route_destination_rank FOREIGN KEY (destination_rank_id) REFERENCES taxi_ranks(id);

ALTER TABLE ONLY routes
    ADD CONSTRAINT fk_route_source_rank FOREIGN KEY (source_rank_id) REFERENCES taxi_ranks(id);

ALTER TABLE ONLY rank_assignment_requests
    ADD CONSTRAINT fkbrqd7qg00ohel9t5k6q8avcyx FOREIGN KEY (rank_id) REFERENCES taxi_ranks(id);

ALTER TABLE ONLY terminals
    ADD CONSTRAINT fkeysjgrkkswcla2xn6jocjnph6 FOREIGN KEY (taxi_rank_id) REFERENCES taxi_ranks(id);

ALTER TABLE ONLY admin_request_ranks
    ADD CONSTRAINT fkg1ur689tif7tgn6at63j5do0 FOREIGN KEY (rank_id) REFERENCES taxi_ranks(id);

ALTER TABLE ONLY admin_request_ranks
    ADD CONSTRAINT fkiesi0kvvd8jq7r32lgxevbdtn FOREIGN KEY (request_id) REFERENCES admin_registration_requests(id);

ALTER TABLE ONLY rank_admins
    ADD CONSTRAINT fkjrllsxfi1vcfi8tu16oncoxfp FOREIGN KEY (rank_id) REFERENCES taxi_ranks(id);

ALTER TABLE ONLY admin_registration_requests
    ADD CONSTRAINT fkknx55ajjxa2mjvv8aou1yv4cv FOREIGN KEY (reviewed_by) REFERENCES users(id);

ALTER TABLE ONLY rank_assignment_requests
    ADD CONSTRAINT fko9q9gq2juaw0eu63r6lhb9nmo FOREIGN KEY (admin_id) REFERENCES rank_admins(id);

ALTER TABLE ONLY rank_admins
    ADD CONSTRAINT fkrnl1hdq4huof0rb791tk5alpx FOREIGN KEY (user_id) REFERENCES users(id);


