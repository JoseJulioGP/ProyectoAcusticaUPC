-- =====================================================================
-- V3 — Zonas iniciales de la UPC (basadas en archivos del sonómetro)
-- =====================================================================

INSERT INTO zones (name, description, building, floor, sector, subsector, active) VALUES
('Primer Piso - Pasillo Principal',          'Zona común de circulación del primer piso',         'Bloque Académico',   'PRIMER_PISO',  'B_TRANQUILIDAD_RUIDO_MODERADO',  'UNIVERSIDADES_COLEGIOS',  TRUE),
('Segundo Piso - Pasillo Principal',         'Zona común de circulación del segundo piso',        'Bloque Académico',   'SEGUNDO_PISO', 'B_TRANQUILIDAD_RUIDO_MODERADO',  'UNIVERSIDADES_COLEGIOS',  TRUE),
('Oficina Seguridad y Salud en el Trabajo',  'Oficina del bloque administrativo',                  'Bloque Administrativo', 'PRIMER_PISO', 'C_RUIDO_INTERMEDIO_RESTRINGIDO', 'OFICINAS_INSTITUCIONAL',  TRUE),
('Oficina SEMPRE',                            'Oficina del bloque administrativo',                  'Bloque Administrativo', 'PRIMER_PISO', 'C_RUIDO_INTERMEDIO_RESTRINGIDO', 'OFICINAS_INSTITUCIONAL',  TRUE),
('Facultad Derecho y Ciencias Políticas',     'Oficinas y aulas del programa de derecho',           'Bloque Derecho',      'PRIMER_PISO', 'B_TRANQUILIDAD_RUIDO_MODERADO',  'UNIVERSIDADES_COLEGIOS',  TRUE);