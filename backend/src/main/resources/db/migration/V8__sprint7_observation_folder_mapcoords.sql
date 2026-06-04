-- =====================================================================
-- V8 — Sprint 7: observación por batch, carpetas de ingesta y
--      coordenadas de plano en zonas.
-- =====================================================================

-- 1) (B1) Observación libre por batch
ALTER TABLE measurement_batches
    ADD COLUMN observation TEXT;

-- 2) (B2) Carpetas de ingesta jerárquicas
CREATE TABLE ingestion_folders (
    id          UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    name        VARCHAR(120) NOT NULL,
    parent_id   UUID         REFERENCES ingestion_folders(id) ON DELETE RESTRICT,
    created_by  UUID         REFERENCES users(id),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_folder_name_parent UNIQUE (name, parent_id)
);

-- Carpeta raíz por defecto
INSERT INTO ingestion_folders (name, parent_id) VALUES ('Sin clasificar', NULL);

-- Vincular batches a carpeta (FK opcional, no rompe batches existentes)
ALTER TABLE measurement_batches
    ADD COLUMN folder_id UUID REFERENCES ingestion_folders(id) ON DELETE SET NULL;

-- Backfill: batches existentes -> "Sin clasificar"
UPDATE measurement_batches
SET folder_id = (SELECT id FROM ingestion_folders
                 WHERE name = 'Sin clasificar' AND parent_id IS NULL)
WHERE folder_id IS NULL;

CREATE INDEX idx_batches_folder ON measurement_batches (folder_id);

-- 3) (B6) Coordenadas de plano por zona (% relativo al contenedor)
ALTER TABLE zones
    ADD COLUMN map_x      DOUBLE PRECISION,
    ADD COLUMN map_y      DOUBLE PRECISION,
    ADD COLUMN map_width  DOUBLE PRECISION,
    ADD COLUMN map_height DOUBLE PRECISION;
