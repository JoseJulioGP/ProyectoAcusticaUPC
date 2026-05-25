package com.upc.acusticupc.ingestion.application.dto;

import com.upc.acusticupc.ingestion.domain.model.ParsedMeasurement;

import java.util.List;

/**
 * Resultado de parsear un archivo del sonómetro.
 *
 * @param measurements  lista de mediciones válidas extraídas
 * @param rejectedRows  filas que se ignoraron por estar mal formadas (vacías, fechas inválidas, valores no numéricos)
 */
public record ParseResult(List<ParsedMeasurement> measurements, int rejectedRows) {

    /** Total de filas que leyó el parser (válidas + rechazadas). */
    public int totalRows() {
        return measurements.size() + rejectedRows;
    }
}