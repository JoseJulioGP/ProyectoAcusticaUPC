package com.upc.acusticupc.reports.domain.dto;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Datos consolidados que se renderizan en el PDF de cumplimiento Res. 627.
 * Se compone reutilizando los servicios de analytics y compliance; este
 * record es solo el contenedor que viaja al generador de PDF.
 */
public record ComplianceReportData(
        OffsetDateTime generatedAt,
        OffsetDateTime from,
        OffsetDateTime to,
        String zoneFilterLabel,        // "Todas las zonas" o el nombre de la zona
        long totalMeasurements,
        int activeZones,
        int totalAlerts,
        double complianceRatePercent,
        List<ZoneComplianceRow> zoneRows,
        List<AlertRow> alerts
) {
    public record ZoneComplianceRow(
            String zoneName,
            String period,             // DIURNO / NOCTURNO
            double laeqDb,
            double standardDb,
            String status              // CUMPLE / EXCEDE
    ) {}

    public record AlertRow(
            String zoneName,
            String period,
            double laeqDb,
            double standardDb,
            double excessDb,
            String severity,
            OffsetDateTime triggeredAt
    ) {}
}