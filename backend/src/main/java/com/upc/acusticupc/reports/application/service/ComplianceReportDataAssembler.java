package com.upc.acusticupc.reports.application.service;

import com.upc.acusticupc.analytics.application.service.AlertStatsService;
import com.upc.acusticupc.analytics.application.service.KpiService;
import com.upc.acusticupc.analytics.application.service.ZoneComparisonService;
import com.upc.acusticupc.analytics.domain.dto.DashboardKpisDTO;
import com.upc.acusticupc.analytics.domain.dto.ZoneComparisonDTO;
import com.upc.acusticupc.compliance.application.dto.AlertDTO;
import com.upc.acusticupc.reports.domain.dto.ComplianceReportData;
import com.upc.acusticupc.sonometry.domain.model.Period;
import com.upc.acusticupc.zones.domain.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ComplianceReportDataAssembler {

    private final KpiService kpiService;
    private final ZoneComparisonService zoneComparisonService;
    private final AlertStatsService alertStatsService;
    private final ZoneRepository zoneRepository;

    public ComplianceReportData assemble(OffsetDateTime from, OffsetDateTime to, UUID zoneId) {
        String label = (zoneId == null)
                ? "Todas las zonas"
                : zoneRepository.findById(zoneId)
                    .map(z -> z.getName())
                    .orElseThrow(() -> new IllegalArgumentException("Zona no encontrada: " + zoneId));

        // 1) KPIs: misma fuente que el dashboard
        DashboardKpisDTO kpis = kpiService.computeKpis(from, to, zoneId, null);

        // 2) Filas por zona/periodo: reutiliza ZoneComparisonService
        List<UUID> zoneFilter = (zoneId != null) ? List.of(zoneId) : null;
        List<ZoneComparisonDTO> comparisons = zoneComparisonService.compare(from, to, zoneFilter, null);
        List<ComplianceReportData.ZoneComplianceRow> rows = toRows(comparisons);

        // 3) Alertas recientes en el rango
        var summary = alertStatsService.summary(from, to);
        List<ComplianceReportData.AlertRow> alertRows = toAlertRows(summary.recent());

        return new ComplianceReportData(
                OffsetDateTime.now(),
                from, to, label,
                kpis.totalMeasurements(),
                kpis.zonesActive(),
                (int) kpis.alertsActive(),
                kpis.complianceRatePercent(),
                rows,
                alertRows
        );
    }

    private List<ComplianceReportData.ZoneComplianceRow> toRows(List<ZoneComparisonDTO> comparisons) {
        List<ComplianceReportData.ZoneComplianceRow> rows = new ArrayList<>();
        for (ZoneComparisonDTO c : comparisons) {
            if (c.laeqDiurnoDb() != null) {
                String status = (c.laeqDiurnoDb() > c.standardDayDb()) ? "EXCEDE" : "CUMPLE";
                rows.add(new ComplianceReportData.ZoneComplianceRow(
                        c.zoneName(), Period.DIURNO.name(),
                        c.laeqDiurnoDb(), c.standardDayDb(), status));
            }
            if (c.laeqNocturnoDb() != null) {
                String status = (c.laeqNocturnoDb() > c.standardNightDb()) ? "EXCEDE" : "CUMPLE";
                rows.add(new ComplianceReportData.ZoneComplianceRow(
                        c.zoneName(), Period.NOCTURNO.name(),
                        c.laeqNocturnoDb(), c.standardNightDb(), status));
            }
        }
        return rows;
    }

    private List<ComplianceReportData.AlertRow> toAlertRows(List<AlertDTO> alerts) {
        return alerts.stream().map(a -> new ComplianceReportData.AlertRow(
                a.zoneName(),
                a.period().name(),
                a.measuredDb().doubleValue(),
                a.standardDb().doubleValue(),
                a.excessDb() != null ? a.excessDb().doubleValue() : 0.0,
                a.severity().name(),
                a.triggeredAt()
        )).toList();
    }
}
