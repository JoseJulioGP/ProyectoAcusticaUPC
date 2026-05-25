package com.upc.acusticupc.zones.domain.model;

/**
 * Subsectores definidos por la Resolución 0627 de 2006 — Tablas 1 y 2.
 * Agrupados por Sector al que pertenecen.
 */
public enum Subsector {

    // Sector A — Tranquilidad y Silencio
    BIBLIOTECAS_HOSPITALES,

    // Sector B — Tranquilidad y Ruido Moderado
    RESIDENCIAL_HOTELERIA,
    UNIVERSIDADES_COLEGIOS,
    PARQUES_URBANOS,

    // Sector C — Ruido Intermedio Restringido
    INDUSTRIAL,
    COMERCIAL,
    OFICINAS_INSTITUCIONAL,
    OTROS_USOS_AIRE_LIBRE,

    // Sector D — Suburbana o Rural de Tranquilidad y Ruido Moderado
    RESIDENCIAL_SUBURBANA,
    RURAL_AGROPECUARIA,
    RECREACION_NATURAL
}