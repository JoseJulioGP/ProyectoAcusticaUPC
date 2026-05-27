-- V6: Indices de performance para el modulo analytics (Sprint 4)
--
-- Estado encontrado al iniciar Sprint 4:
--   measurements: PK + idx_measurements_zone_time (zone_id, measured_at) + idx_measurements_period
--   alerts: PK + 5 indices (batch_id, compliance_result_id, severity, triggered_at, zone_id)
--   compliance_results: PK + 4 indices (batch_id, evaluated_at, status, zone_id)
--   measurement_batches: PK + idx_batches_status
--
-- V6 solo agrega 2 indices realmente nuevos.

-- 1. measurements: indice por measured_at suelto (queries globales sin filtro de zona).
CREATE INDEX IF NOT EXISTS idx_measurements_measured_at
    ON measurements (measured_at);

-- 2. measurement_batches: compuesto status + uploaded_at para "ultimos COMPLETED por fecha".
CREATE INDEX IF NOT EXISTS idx_batches_status_uploaded
    ON measurement_batches (status, uploaded_at DESC);