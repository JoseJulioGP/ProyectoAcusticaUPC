package com.upc.acusticupc.compliance.application.dto;

import com.upc.acusticupc.compliance.domain.model.Alert;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AlertResponse(
        UUID id,
        UUID zoneId,
        String zoneName,
        OffsetDateTime when,
        String period,
        double measuredDb,
        double standardDb,
        double excessDb,
        String severity,
        String observation
) {
    public static AlertResponse from(Alert a) {
        double measured = a.getMeasuredDb().doubleValue();
        double standard = a.getStandardDb().doubleValue();
        // excessDb es columna generada en la BD; si viniera null (p. ej. en H2) se recalcula.
        double excess = (a.getExcessDb() != null) ? a.getExcessDb().doubleValue() : measured - standard;
        // Sprint 8: observation del batch que disparó la alerta (puede no estar ligada a batch).
        String observation = (a.getBatch() != null) ? a.getBatch().getObservation() : null;
        return new AlertResponse(
                a.getId(),
                a.getZone().getId(),
                a.getZone().getName(),
                a.getTriggeredAt(),
                a.getPeriod().name(),
                measured,
                standard,
                excess,
                a.getSeverity().name(),
                observation
        );
    }
}
