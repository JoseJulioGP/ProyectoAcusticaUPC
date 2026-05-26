-- =====================================================================
-- V4 — Completar Resolución 627/2006 (Sectores B, C y D faltantes)
-- =====================================================================
-- V2 sembró las dos tablas parcialmente: Sector A completo, B y C con
-- solo algunos subsectores, Sector D ausente. V4 completa lo que falta
-- para que el motor de cumplimiento pueda evaluar cualquier zona de la
-- Resolución 627 sin huecos.
--
-- Filas que agrega V4: 30 (14 AMBIENT + 16 EMISSION).
-- Total tras V4: 44 noise_standards (14 previos + 30 nuevos).

-- ---------------------------------------------------------------------
-- TABLA 2 — Ruido AMBIENTAL (Art. 17)
-- ---------------------------------------------------------------------

-- Sector B - completar RESIDENCIAL_HOTELERIA (UNIVERSIDADES y PARQUES ya sembrados en V2)
INSERT INTO noise_standards (sector, subsector, period, standard_type, max_db, regulation) VALUES
('B_TRANQUILIDAD_RUIDO_MODERADO',  'RESIDENCIAL_HOTELERIA', 'DIURNO',   'AMBIENT', 65, 'Res 0627/2006 Tabla 2'),
('B_TRANQUILIDAD_RUIDO_MODERADO',  'RESIDENCIAL_HOTELERIA', 'NOCTURNO', 'AMBIENT', 50, 'Res 0627/2006 Tabla 2');

-- Sector C - completar INDUSTRIAL, COMERCIAL, OTROS_USOS_AIRE_LIBRE (OFICINAS ya en V2)
INSERT INTO noise_standards (sector, subsector, period, standard_type, max_db, regulation) VALUES
('C_RUIDO_INTERMEDIO_RESTRINGIDO', 'INDUSTRIAL',            'DIURNO',   'AMBIENT', 75, 'Res 0627/2006 Tabla 2'),
('C_RUIDO_INTERMEDIO_RESTRINGIDO', 'INDUSTRIAL',            'NOCTURNO', 'AMBIENT', 70, 'Res 0627/2006 Tabla 2'),
('C_RUIDO_INTERMEDIO_RESTRINGIDO', 'COMERCIAL',             'DIURNO',   'AMBIENT', 70, 'Res 0627/2006 Tabla 2'),
('C_RUIDO_INTERMEDIO_RESTRINGIDO', 'COMERCIAL',             'NOCTURNO', 'AMBIENT', 55, 'Res 0627/2006 Tabla 2'),
('C_RUIDO_INTERMEDIO_RESTRINGIDO', 'OTROS_USOS_AIRE_LIBRE', 'DIURNO',   'AMBIENT', 80, 'Res 0627/2006 Tabla 2'),
('C_RUIDO_INTERMEDIO_RESTRINGIDO', 'OTROS_USOS_AIRE_LIBRE', 'NOCTURNO', 'AMBIENT', 70, 'Res 0627/2006 Tabla 2');

-- Sector D - sembrar completo (no estaba en V2)
INSERT INTO noise_standards (sector, subsector, period, standard_type, max_db, regulation) VALUES
('D_SUBURBANA_RURAL',              'RESIDENCIAL_SUBURBANA', 'DIURNO',   'AMBIENT', 55, 'Res 0627/2006 Tabla 2'),
('D_SUBURBANA_RURAL',              'RESIDENCIAL_SUBURBANA', 'NOCTURNO', 'AMBIENT', 45, 'Res 0627/2006 Tabla 2'),
('D_SUBURBANA_RURAL',              'RURAL_AGROPECUARIA',    'DIURNO',   'AMBIENT', 55, 'Res 0627/2006 Tabla 2'),
('D_SUBURBANA_RURAL',              'RURAL_AGROPECUARIA',    'NOCTURNO', 'AMBIENT', 45, 'Res 0627/2006 Tabla 2'),
('D_SUBURBANA_RURAL',              'RECREACION_NATURAL',    'DIURNO',   'AMBIENT', 55, 'Res 0627/2006 Tabla 2'),
('D_SUBURBANA_RURAL',              'RECREACION_NATURAL',    'NOCTURNO', 'AMBIENT', 45, 'Res 0627/2006 Tabla 2');

-- ---------------------------------------------------------------------
-- TABLA 1 — EMISIÓN (Art. 9)
-- ---------------------------------------------------------------------

-- Sector B - completar RESIDENCIAL_HOTELERIA y PARQUES_URBANOS (UNIVERSIDADES ya en V2)
INSERT INTO noise_standards (sector, subsector, period, standard_type, max_db, regulation) VALUES
('B_TRANQUILIDAD_RUIDO_MODERADO',  'RESIDENCIAL_HOTELERIA', 'DIURNO',   'EMISSION', 65, 'Res 0627/2006 Tabla 1'),
('B_TRANQUILIDAD_RUIDO_MODERADO',  'RESIDENCIAL_HOTELERIA', 'NOCTURNO', 'EMISSION', 55, 'Res 0627/2006 Tabla 1'),
('B_TRANQUILIDAD_RUIDO_MODERADO',  'PARQUES_URBANOS',       'DIURNO',   'EMISSION', 65, 'Res 0627/2006 Tabla 1'),
('B_TRANQUILIDAD_RUIDO_MODERADO',  'PARQUES_URBANOS',       'NOCTURNO', 'EMISSION', 55, 'Res 0627/2006 Tabla 1');

-- Sector C - completar INDUSTRIAL, COMERCIAL, OTROS_USOS_AIRE_LIBRE (OFICINAS ya en V2)
INSERT INTO noise_standards (sector, subsector, period, standard_type, max_db, regulation) VALUES
('C_RUIDO_INTERMEDIO_RESTRINGIDO', 'INDUSTRIAL',            'DIURNO',   'EMISSION', 75, 'Res 0627/2006 Tabla 1'),
('C_RUIDO_INTERMEDIO_RESTRINGIDO', 'INDUSTRIAL',            'NOCTURNO', 'EMISSION', 75, 'Res 0627/2006 Tabla 1'),
('C_RUIDO_INTERMEDIO_RESTRINGIDO', 'COMERCIAL',             'DIURNO',   'EMISSION', 70, 'Res 0627/2006 Tabla 1'),
('C_RUIDO_INTERMEDIO_RESTRINGIDO', 'COMERCIAL',             'NOCTURNO', 'EMISSION', 60, 'Res 0627/2006 Tabla 1'),
('C_RUIDO_INTERMEDIO_RESTRINGIDO', 'OTROS_USOS_AIRE_LIBRE', 'DIURNO',   'EMISSION', 80, 'Res 0627/2006 Tabla 1'),
('C_RUIDO_INTERMEDIO_RESTRINGIDO', 'OTROS_USOS_AIRE_LIBRE', 'NOCTURNO', 'EMISSION', 75, 'Res 0627/2006 Tabla 1');

-- Sector D - sembrar completo (no estaba en V2)
INSERT INTO noise_standards (sector, subsector, period, standard_type, max_db, regulation) VALUES
('D_SUBURBANA_RURAL',              'RESIDENCIAL_SUBURBANA', 'DIURNO',   'EMISSION', 55, 'Res 0627/2006 Tabla 1'),
('D_SUBURBANA_RURAL',              'RESIDENCIAL_SUBURBANA', 'NOCTURNO', 'EMISSION', 50, 'Res 0627/2006 Tabla 1'),
('D_SUBURBANA_RURAL',              'RURAL_AGROPECUARIA',    'DIURNO',   'EMISSION', 55, 'Res 0627/2006 Tabla 1'),
('D_SUBURBANA_RURAL',              'RURAL_AGROPECUARIA',    'NOCTURNO', 'EMISSION', 50, 'Res 0627/2006 Tabla 1'),
('D_SUBURBANA_RURAL',              'RECREACION_NATURAL',    'DIURNO',   'EMISSION', 55, 'Res 0627/2006 Tabla 1'),
('D_SUBURBANA_RURAL',              'RECREACION_NATURAL',    'NOCTURNO', 'EMISSION', 50, 'Res 0627/2006 Tabla 1');