package com.upc.acusticupc.reports.application.service;

import com.upc.acusticupc.reports.domain.dto.ComplianceReportData;
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

    // private final KpiService kpiService;
    // private final ZoneComparisonService zoneComparisonService;
    // private final AlertStatsService alertStatsService;
    private final ZoneRepository zoneRepository;

    public ComplianceReportData assemble(OffsetDateTime from, OffsetDateTime to, UUID zoneId) {
        String label = (zoneId == null)
                ? "Todas las zonas"
                : zoneRepository.findById(zoneId)
                    .map(z -> z.getName())
                    .orElseThrow(() -> new IllegalArgumentException("Zona no encontrada: " + zoneId));

        // 1) KPIs: reutiliza KpiService (mismo cálculo que el dashboard).
        // var kpis = kpiService.computeKpis(from, to, zoneId, null);

        // 2) Filas por zona/periodo: reutiliza ZoneComparisonService.
        List<ComplianceReportData.ZoneComplianceRow> rows = new ArrayList<>();
        // for (var c : zoneComparisonService.compare(from, to, ...)) {
        //     rows.add(new ComplianceReportData.ZoneComplianceRow(
        //         c.zoneName(), "DIURNO", c.laeqDayDb(), c.standardDayDb(),
        //         c.laeqDayDb() > c.standardDayDb() ? "EXCEDE" : "CUMPLE"));
        //     ... idem NOCTURNO ...
        // }

        // 3) Alertas: reutiliza AlertStatsService o el repo de alertas.
        List<ComplianceReportData.AlertRow> alerts = new ArrayList<>();
        // for (var a : alertStatsService.list(from, to, zoneId)) { alerts.add(...); }

        return new ComplianceReportData(
                OffsetDateTime.now(),
                from, to, label,
                0L,    // kpis.totalMeasurements()
                0,     // kpis.activeZones()
                alerts.size(),
                0.0,   // kpis.complianceRatePercent()
                rows,
                alerts
        );
    }
}