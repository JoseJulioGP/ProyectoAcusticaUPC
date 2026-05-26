package com.upc.acusticupc.analytics.domain.dto;

import com.upc.acusticupc.compliance.domain.model.AlertSeverity;

import java.time.OffsetDateTime;
import java.util.Map;

public record DashboardKpisDTO(
        long totalMeasurements,
        int zonesActive,
        long alertsActive,
        Map<AlertSeverity, Long> alertsBySeverity,
        double complianceRatePercent,
        OffsetDateTime from,
        OffsetDateTime to
) {}