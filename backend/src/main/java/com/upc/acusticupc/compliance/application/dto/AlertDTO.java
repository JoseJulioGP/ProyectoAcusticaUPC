package com.upc.acusticupc.compliance.application.dto;

import com.upc.acusticupc.compliance.domain.model.Alert;
import com.upc.acusticupc.compliance.domain.model.AlertSeverity;
import com.upc.acusticupc.sonometry.domain.model.Period;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AlertDTO(
        UUID id,
        UUID zoneId,
        String zoneName,
        UUID batchId,
        Period period,
        BigDecimal measuredDb,
        BigDecimal standardDb,
        BigDecimal excessDb,
        AlertSeverity severity,
        OffsetDateTime triggeredAt,
        String notes
) {
    public static AlertDTO from(Alert a) {
        return new AlertDTO(
                a.getId(),
                a.getZone().getId(),
                a.getZone().getName(),
                a.getBatch() != null ? a.getBatch().getId() : null,
                a.getPeriod(),
                a.getMeasuredDb(),
                a.getStandardDb(),
                a.getExcessDb(),
                a.getSeverity(),
                a.getTriggeredAt(),
                a.getNotes()
        );
    }
}
