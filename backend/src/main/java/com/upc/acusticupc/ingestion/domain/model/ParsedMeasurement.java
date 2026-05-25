package com.upc.acusticupc.ingestion.domain.model;

import java.time.LocalDateTime;

/**
 * Una medicion leida de un archivo del sonometro, antes de persistirse.
 * Inmutable. Output del parser, input del servicio de ingesta.
 */
public record ParsedMeasurement(
        double dbValue,
        String unit,
        LocalDateTime capturedAt,
        String sourceSheet
) {}
