package com.upc.acusticupc.analytics.domain.dto;

import com.upc.acusticupc.compliance.domain.model.ComplianceStatus;

import java.util.UUID;

public record ZoneStatsDTO(
        UUID zoneId,
        String zoneName,
        String sector,
        String subsector,
        long measurements,
        Double laeqDiurnoDb,      // null si no hay datos diurnos en rango
        Double laeqNocturnoDb,
        double standardDayDb,
        double standardNightDb,
        long alertsCount,
        ComplianceStatus overallStatus
) {}