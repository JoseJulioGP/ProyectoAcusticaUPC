-- =====================================================================
-- V5 — Tablas fisicas del motor de cumplimiento Res. 627
-- =====================================================================
-- Crea las dos tablas que consume el ComplianceEngine:
--  1. compliance_results: una fila por (batch, period) con LAeq/L90/min/max
--     y veredicto (CUMPLE / NO_CUMPLE).
--  2. alerts: una fila por cada compliance_result NO_CUMPLE, con la
--     severidad calculada por AlertSeverity.fromExcess().
--
-- ORDEN: compliance_results PRIMERO, alerts DESPUES (alerts tiene FK
-- hacia compliance_results).
--
-- NOTA sobre excess_db en alerts: es columna GENERADA por la BD
-- (measured_db - standard_db). La entidad Alert.java la mapea con
-- insertable=false, updatable=false: Hibernate no la escribe, solo la lee.

-- ---------------------------------------------------------------------
-- TABLA compliance_results
-- ---------------------------------------------------------------------
CREATE TABLE compliance_results (
    id                  UUID            PRIMARY KEY,
    zone_id             UUID            NOT NULL REFERENCES zones(id),
    batch_id            UUID            REFERENCES measurement_batches(id),
    period              VARCHAR(255)    NOT NULL,
    measurement_count   INTEGER         NOT NULL,
    laeq_db             NUMERIC(5, 2)   NOT NULL,
    l90_db              NUMERIC(5, 2)   NOT NULL,
    min_db              NUMERIC(5, 2)   NOT NULL,
    max_db              NUMERIC(5, 2)   NOT NULL,
    standard_db         NUMERIC(5, 2)   NOT NULL,
    status              VARCHAR(255)    NOT NULL,
    evaluated_from      TIMESTAMPTZ     NOT NULL,
    evaluated_to        TIMESTAMPTZ     NOT NULL,
    evaluated_at        TIMESTAMPTZ     NOT NULL,
    standard_type       VARCHAR(16)     NOT NULL
);

-- Indices para queries que vendran del ComplianceController (Dev 2):
--  - listar por zona y rango de fechas
--  - listar por batch
--  - filtrar por status (CUMPLE / NO_CUMPLE)
CREATE INDEX idx_compliance_results_zone_id      ON compliance_results(zone_id);
CREATE INDEX idx_compliance_results_batch_id     ON compliance_results(batch_id);
CREATE INDEX idx_compliance_results_evaluated_at ON compliance_results(evaluated_at DESC);
CREATE INDEX idx_compliance_results_status       ON compliance_results(status);

-- ---------------------------------------------------------------------
-- TABLA alerts
-- ---------------------------------------------------------------------
CREATE TABLE alerts (
    id                      UUID            PRIMARY KEY,
    zone_id                 UUID            NOT NULL REFERENCES zones(id),
    batch_id                UUID            REFERENCES measurement_batches(id),
    compliance_result_id    UUID            REFERENCES compliance_results(id),
    period                  VARCHAR(255)    NOT NULL,
    measured_db             NUMERIC(5, 2)   NOT NULL,
    standard_db             NUMERIC(5, 2)   NOT NULL,
    excess_db               NUMERIC(5, 2)   GENERATED ALWAYS AS (measured_db - standard_db) STORED,
    severity                VARCHAR(255)    NOT NULL,
    triggered_at            TIMESTAMPTZ     NOT NULL,
    notes                   TEXT
);

-- Indices para queries que vendran del ComplianceController (Dev 2):
--  - listar por zona y rango de fechas
--  - filtrar por severidad (LEVE / MODERADA / CRITICA)
--  - join con compliance_result si UI muestra el detalle
CREATE INDEX idx_alerts_zone_id              ON alerts(zone_id);
CREATE INDEX idx_alerts_batch_id             ON alerts(batch_id);
CREATE INDEX idx_alerts_triggered_at         ON alerts(triggered_at DESC);
CREATE INDEX idx_alerts_severity             ON alerts(severity);
CREATE INDEX idx_alerts_compliance_result_id ON alerts(compliance_result_id);