package com.upc.acusticupc.analytics.application.service;

import com.upc.acusticupc.analytics.domain.dto.DashboardKpisDTO;
import com.upc.acusticupc.analytics.domain.repository.AlertStatsRepository;
import com.upc.acusticupc.analytics.domain.repository.MeasurementStatsRepository;
import com.upc.acusticupc.compliance.domain.model.AlertSeverity;
import com.upc.acusticupc.compliance.domain.model.ComplianceStatus;
import com.upc.acusticupc.compliance.domain.repository.ComplianceResultRepository;
import com.upc.acusticupc.shared.config.CacheConfig;
import com.upc.acusticupc.sonometry.domain.model.Period;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KpiService {

    private final MeasurementStatsRepository measurementStatsRepository;
    private final AlertStatsRepository alertStatsRepository;
    private final ComplianceResultRepository complianceResultRepository;

    @Cacheable(
            value = CacheConfig.KPIS_CACHE,
            key = "T(java.util.Objects).hash(#from, #to, #zoneId, #period)"
    )
    @Transactional(readOnly = true)
    public DashboardKpisDTO computeKpis(OffsetDateTime from,
                                        OffsetDateTime to,
                                        UUID zoneId,
                                        Period period) {

        long totalMeasurements = measurementStatsRepository.countInRange(from, to, zoneId, period);
        int zonesActive = measurementStatsRepository.countActiveZones(from, to);

        // Alertas
        Map<AlertSeverity, Long> bySeverity = new EnumMap<>(AlertSeverity.class);
        for (AlertSeverity s : AlertSeverity.values()) bySeverity.put(s, 0L);

        for (Object[] row : alertStatsRepository.countBySeverityInRange(from, to)) {
            AlertSeverity sev = (AlertSeverity) row[0];
            Long count = ((Number) row[1]).longValue();
            bySeverity.put(sev, count);
        }
        long alertsActive = bySeverity.values().stream().mapToLong(Long::longValue).sum();

        // % cumplimiento: CUMPLE / total ComplianceResults en rango
        long totalResults = complianceResultRepository.countByEvaluatedAtBetween(from, to);
        long cumple = complianceResultRepository.countByEvaluatedAtBetweenAndStatus(
                from, to, ComplianceStatus.CUMPLE);

        double rate = totalResults == 0 ? 0.0 : (100.0 * cumple / totalResults);

        return new DashboardKpisDTO(
                totalMeasurements,
                zonesActive,
                alertsActive,
                bySeverity,
                round1(rate),
                from,
                to
        );
    }

    private double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }
}