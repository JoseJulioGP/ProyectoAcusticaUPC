-- =====================================================================
-- V2 — Siembra Resolución 627/2006 (Tabla 1 y Tabla 2)
-- =====================================================================

-- TABLA 2 — Ruido AMBIENTAL (lo que se mide en el campus)
INSERT INTO noise_standards (sector, subsector, period, standard_type, max_db, regulation) VALUES
('B_TRANQUILIDAD_RUIDO_MODERADO', 'UNIVERSIDADES_COLEGIOS', 'DIURNO',   'AMBIENT', 65, 'Res 0627/2006 Tabla 2'),
('B_TRANQUILIDAD_RUIDO_MODERADO', 'UNIVERSIDADES_COLEGIOS', 'NOCTURNO', 'AMBIENT', 50, 'Res 0627/2006 Tabla 2'),
('B_TRANQUILIDAD_RUIDO_MODERADO', 'PARQUES_URBANOS',        'DIURNO',   'AMBIENT', 65, 'Res 0627/2006 Tabla 2'),
('B_TRANQUILIDAD_RUIDO_MODERADO', 'PARQUES_URBANOS',        'NOCTURNO', 'AMBIENT', 50, 'Res 0627/2006 Tabla 2');

INSERT INTO noise_standards (sector, subsector, period, standard_type, max_db, regulation) VALUES
('A_TRANQUILIDAD_SILENCIO', 'BIBLIOTECAS_HOSPITALES', 'DIURNO',   'AMBIENT', 55, 'Res 0627/2006 Tabla 2'),
('A_TRANQUILIDAD_SILENCIO', 'BIBLIOTECAS_HOSPITALES', 'NOCTURNO', 'AMBIENT', 45, 'Res 0627/2006 Tabla 2');

INSERT INTO noise_standards (sector, subsector, period, standard_type, max_db, regulation) VALUES
('C_RUIDO_INTERMEDIO_RESTRINGIDO', 'OFICINAS_INSTITUCIONAL', 'DIURNO',   'AMBIENT', 65, 'Res 0627/2006 Tabla 2'),
('C_RUIDO_INTERMEDIO_RESTRINGIDO', 'OFICINAS_INSTITUCIONAL', 'NOCTURNO', 'AMBIENT', 50, 'Res 0627/2006 Tabla 2');

-- TABLA 1 — EMISIÓN (de fuentes específicas)
INSERT INTO noise_standards (sector, subsector, period, standard_type, max_db, regulation) VALUES
('B_TRANQUILIDAD_RUIDO_MODERADO',   'UNIVERSIDADES_COLEGIOS', 'DIURNO',   'EMISSION', 65, 'Res 0627/2006 Tabla 1'),
('B_TRANQUILIDAD_RUIDO_MODERADO',   'UNIVERSIDADES_COLEGIOS', 'NOCTURNO', 'EMISSION', 55, 'Res 0627/2006 Tabla 1'),
('C_RUIDO_INTERMEDIO_RESTRINGIDO',  'OFICINAS_INSTITUCIONAL', 'DIURNO',   'EMISSION', 65, 'Res 0627/2006 Tabla 1'),
('C_RUIDO_INTERMEDIO_RESTRINGIDO',  'OFICINAS_INSTITUCIONAL', 'NOCTURNO', 'EMISSION', 55, 'Res 0627/2006 Tabla 1'),
('A_TRANQUILIDAD_SILENCIO',         'BIBLIOTECAS_HOSPITALES', 'DIURNO',   'EMISSION', 55, 'Res 0627/2006 Tabla 1'),
('A_TRANQUILIDAD_SILENCIO',         'BIBLIOTECAS_HOSPITALES', 'NOCTURNO', 'EMISSION', 50, 'Res 0627/2006 Tabla 1');