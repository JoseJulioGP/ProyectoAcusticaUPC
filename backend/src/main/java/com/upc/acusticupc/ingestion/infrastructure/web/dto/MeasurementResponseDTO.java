package com.upc.acusticupc.ingestion.infrastructure.web.dto;

import com.upc.acusticupc.sonometry.domain.model.Measurement;
import com.upc.acusticupc.sonometry.domain.model.Period;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Versión plana de Measurement para respuestas REST (sin relaciones JPA lazy).
 */
public record MeasurementResponseDTO(
        UUID id,
        Double dbValue,
        String unit,
        OffsetDateTime measuredAt,
        Period period
) {
    public static MeasurementResponseDTO from(Measurement m) {
        return new MeasurementResponseDTO(
                m.getId(),
                m.getDbValue(),
                m.getUnit(),
                m.getMeasuredAt(),
                m.getPeriod()
        );
    }
}