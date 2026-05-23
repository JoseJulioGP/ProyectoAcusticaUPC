-- =====================================================================
-- V1 — Esquema inicial AcústicaUPC
-- =====================================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE users (
    id            UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    full_name     VARCHAR(120) NOT NULL,
    email         VARCHAR(160) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL,
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_email ON users (LOWER(email));

CREATE TABLE zones (
    id          UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    name        VARCHAR(150) NOT NULL,
    description VARCHAR(500),
    building    VARCHAR(100),
    floor       VARCHAR(50),
    sector      VARCHAR(40)  NOT NULL,
    subsector   VARCHAR(40)  NOT NULL,
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_zones_active ON zones (active);

CREATE TABLE measurement_batches (
    id             UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    file_name      VARCHAR(255) NOT NULL,
    zone_id        UUID         NOT NULL REFERENCES zones(id),
    uploaded_by    UUID                 REFERENCES users(id),
    status         VARCHAR(20)  NOT NULL,
    total_rows     INTEGER,
    valid_rows     INTEGER,
    rejected_rows  INTEGER,
    error_message  VARCHAR(1000),
    uploaded_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    processed_at   TIMESTAMPTZ
);

CREATE INDEX idx_batches_status ON measurement_batches (status);

CREATE TABLE measurements (
    id           UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    zone_id      UUID         NOT NULL REFERENCES zones(id),
    batch_id     UUID         NOT NULL REFERENCES measurement_batches(id) ON DELETE CASCADE,
    db_value     DOUBLE PRECISION NOT NULL,
    measured_at  TIMESTAMPTZ  NOT NULL,
    period       VARCHAR(20)  NOT NULL,
    unit         VARCHAR(10)
);

CREATE INDEX idx_measurements_zone_time ON measurements (zone_id, measured_at);
CREATE INDEX idx_measurements_period    ON measurements (period);

CREATE TABLE noise_standards (
    id            UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    sector        VARCHAR(40)  NOT NULL,
    subsector     VARCHAR(40)  NOT NULL,
    period        VARCHAR(20)  NOT NULL,
    standard_type VARCHAR(20)  NOT NULL,
    max_db        DOUBLE PRECISION NOT NULL,
    regulation    VARCHAR(50),
    CONSTRAINT uq_noise_standard UNIQUE (sector, subsector, period, standard_type)
);