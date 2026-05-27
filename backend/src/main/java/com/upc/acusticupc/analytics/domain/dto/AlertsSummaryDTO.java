package com.upc.acusticupc.analytics.domain.dto;

import com.upc.acusticupc.compliance.application.dto.AlertDTO;
import com.upc.acusticupc.compliance.domain.model.AlertSeverity;

import java.util.List;
import java.util.Map;

public record AlertsSummaryDTO(
        long total,
        Map<AlertSeverity, Long> bySeverity,
        Map<String, Long> byZone,
        List<AlertDTO> recent
) {}