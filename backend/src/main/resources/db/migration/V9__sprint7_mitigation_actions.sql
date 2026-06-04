-- Sprint 7 (RF#11) — Catálogo de acciones de mitigación de ruido.
-- Alineado con la Resolución 0627 de 2006 (MAVDT). Borrado lógico vía `active`.

CREATE TABLE mitigation_actions (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    code                VARCHAR(16)  NOT NULL UNIQUE,
    title               VARCHAR(160) NOT NULL,
    description         TEXT         NOT NULL,
    regulation_ref      VARCHAR(120),
    priority            INTEGER      NOT NULL DEFAULT 5,
    estimated_impact_db DOUBLE PRECISION,
    active              BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ
);

-- Índice de soporte para la consulta principal del catálogo:
-- findByActiveTrueOrderByPriorityAsc().
CREATE INDEX idx_mitigation_actions_active_priority
    ON mitigation_actions (active, priority);

-- Semillas iniciales (8 acciones de referencia).
INSERT INTO mitigation_actions (code, title, description, regulation_ref, priority, estimated_impact_db) VALUES
('M01', 'Instalar pantalla acústica',
 'Barrera física entre fuente y receptor. Reduce nivel sonoro directo en línea de vista.',
 'Res. 0627/2006 Art. 26', 1, 8.0),
('M02', 'Reubicar fuente generadora',
 'Mover equipos ruidosos a azoteas, sótanos o zonas técnicas aisladas del receptor sensible.',
 'Res. 0627/2006 Art. 26', 2, 6.0),
('M03', 'Aislar instalaciones de ventilación/climatización',
 'Encapsulamiento acústico de ductos, ventiladores y unidades condensadoras.',
 'Res. 0627/2006 Art. 26 + Anexo 2 (Ks bajas frecuencias)', 2, 5.0),
('M04', 'Restringir horario de operación de fuente',
 'Limitar funcionamiento de equipos ruidosos al horario diurno (07:01–21:00).',
 'Res. 0627/2006 Art. 2 + Art. 9', 3, 4.0),
('M05', 'Calibrar/revisar alarmas',
 'Las alarmas de seguridad no deben exceder 85 dB(A) medidos a 3 m en la dirección de máxima emisión.',
 'Res. 0627/2006 Art. 27', 3, 3.0),
('M06', 'Atenuadores en azoteas/patios',
 'Sistemas de atenuación de ruido para equipos instalados en cubiertas que afecten el ambiente.',
 'Res. 0627/2006 Art. 26 párr. 2', 2, 5.0),
('M07', 'Revisión de aislamiento de edificación',
 'Auditoría de paramentos verticales, puertas y ventanas para identificar puntos de fuga acústica.',
 'Res. 0627/2006 Art. 26', 4, 3.0),
('M08', 'Reubicar actividad receptora',
 'Mover el receptor sensible (aula, biblioteca, oficina) a una zona menos expuesta.',
 'Res. 0627/2006 Art. 9 — sector más restrictivo', 5, 10.0);
