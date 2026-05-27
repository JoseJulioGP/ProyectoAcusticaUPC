package com.upc.acusticupc.analytics.domain.dto;

import com.upc.acusticupc.compliance.application.dto.AlertDTO;
import com.upc.acusticupc.ingestion.application.dto.BatchSummaryDTO;

import java.util.List;
import java.util.UUID;

public record ZoneDetailDTO(
        UUID zoneId,
        String zoneName,
        String sector,
        String subsector,
        String description,
        ZoneStatsDTO stats,
        List<BatchSummaryDTO> recentBatches,
        List<AlertDTO> recentAlerts
) {}