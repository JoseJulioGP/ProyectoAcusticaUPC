package com.upc.acusticupc.analytics.application.service;

import com.upc.acusticupc.analytics.domain.dto.AlertsSummaryDTO;
import com.upc.acusticupc.analytics.domain.repository.AlertStatsRepository;
import com.upc.acusticupc.compliance.application.dto.AlertDTO;
import com.upc.acusticupc.compliance.domain.model.AlertSeverity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AlertStatsService {

    private final AlertStatsRepository alertStatsRepository;

    @Transactional(readOnly = true)
    public AlertsSummaryDTO summary(OffsetDateTime from, OffsetDateTime to) {

        long total = alertStatsRepository.countByTriggeredAtBetween(from, to);

        Map<AlertSeverity, Long> bySev = new EnumMap<>(AlertSeverity.class);
        for (AlertSeverity s : AlertSeverity.values()) bySev.put(s, 0L);
        for (Object[] row : alertStatsRepository.countBySeverityInRange(from, to)) {
            bySev.put((AlertSeverity) row[0], ((Number) row[1]).longValue());
        }

        Map<String, Long> byZone = new HashMap<>();
        for (Object[] row : alertStatsRepository.countByZoneInRange(from, to)) {
            byZone.put((String) row[0], ((Number) row[1]).longValue());
        }

        var recent = alertStatsRepository
                .findRecentInRange(from, to, PageRequest.of(0, 10))
                .stream()
                .map(AlertDTO::from)
                .toList();

        return new AlertsSummaryDTO(total, bySev, byZone, recent);
    }
}