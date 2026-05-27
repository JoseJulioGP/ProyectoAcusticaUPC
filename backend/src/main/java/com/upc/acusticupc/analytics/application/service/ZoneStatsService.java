package com.upc.acusticupc.analytics.application.service;

import com.upc.acusticupc.analytics.domain.dto.ZoneStatsDTO;
import com.upc.acusticupc.analytics.domain.repository.AlertStatsRepository;
import com.upc.acusticupc.analytics.domain.repository.MeasurementStatsRepository;
import com.upc.acusticupc.compliance.domain.model.ComplianceStatus;
import com.upc.acusticupc.compliance.domain.model.NoiseStandard;
import com.upc.acusticupc.compliance.domain.model.StandardType;
import com.upc.acusticupc.compliance.domain.repository.NoiseStandardRepository;
import com.upc.acusticupc.sonometry.domain.model.Period;
import com.upc.acusticupc.zones.domain.model.Zone;
import com.upc.acusticupc.zones.domain.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ZoneStatsService {

    private final ZoneRepository zoneRepository;
    private final MeasurementStatsRepository measurementStatsRepository;
    private final AlertStatsRepository alertStatsRepository;
    private final NoiseStandardRepository noiseStandardRepository;

    @Transactional(readOnly = true)
    public List<ZoneStatsDTO> statsForAllZones(OffsetDateTime from, OffsetDateTime to) {

        List<Zone> zones = zoneRepository.findAll();

        // laeq por (zone, period) en una sola query
        Map<UUID, Map<Period, Double>> laeqMap = new HashMap<>();
        for (Object[] row : measurementStatsRepository.laeqByZoneAndPeriod(from, to, null)) {
            UUID zoneId = (UUID) row[0];
            Period p = Period.valueOf((String) row[1]);
            double laeq = ((Number) row[2]).doubleValue();
            laeqMap.computeIfAbsent(zoneId, k -> new EnumMap<>(Period.class)).put(p, laeq);
        }

        // Counts por zona
        Map<UUID, Long> countMap = new HashMap<>();
        for (Object[] row : measurementStatsRepository.measurementCountByZone(from, to)) {
            countMap.put((UUID) row[0], ((Number) row[1]).longValue());
        }

        // Counts de alertas por zona
        Map<String, Long> alertsByZoneName = new HashMap<>();
        for (Object[] row : alertStatsRepository.countByZoneInRange(from, to)) {
            alertsByZoneName.put((String) row[0], ((Number) row[1]).longValue());
        }

        return zones.stream().map(z -> {
            Map<Period, Double> laeqByPeriod = laeqMap.getOrDefault(z.getId(), Map.of());
            Double laeqDia = laeqByPeriod.get(Period.DIURNO);
            Double laeqNoche = laeqByPeriod.get(Period.NOCTURNO);

            NoiseStandard stdDia = noiseStandardRepository
                    .findBySectorAndSubsectorAndPeriodAndStandardType(
                            z.getSector(), z.getSubsector(), Period.DIURNO, StandardType.AMBIENT)
                    .orElseThrow(() -> new IllegalStateException(
                            "No hay estandar AMBIENTAL DIURNO para zona " + z.getName()));

            NoiseStandard stdNoche = noiseStandardRepository
                    .findBySectorAndSubsectorAndPeriodAndStandardType(
                            z.getSector(), z.getSubsector(), Period.NOCTURNO, StandardType.AMBIENT)
                    .orElseThrow(() -> new IllegalStateException(
                            "No hay estandar AMBIENTAL NOCTURNO para zona " + z.getName()));

            ComplianceStatus overall = determineOverallStatus(laeqDia, laeqNoche, stdDia, stdNoche);

            return new ZoneStatsDTO(
                    z.getId(),
                    z.getName(),
                    z.getSector().name(),
                    z.getSubsector().name(),
                    countMap.getOrDefault(z.getId(), 0L),
                    laeqDia != null ? round1(laeqDia) : null,
                    laeqNoche != null ? round1(laeqNoche) : null,
                    stdDia.getMaxDb(),
                    stdNoche.getMaxDb(),
                    alertsByZoneName.getOrDefault(z.getName(), 0L),
                    overall
            );
        }).toList();
    }

    private ComplianceStatus determineOverallStatus(Double dia, Double noche,
                                                    NoiseStandard stdDia, NoiseStandard stdNoche) {
        boolean diaCumple = dia == null || dia <= stdDia.getMaxDb();
        boolean nocheCumple = noche == null || noche <= stdNoche.getMaxDb();
        return (diaCumple && nocheCumple) ? ComplianceStatus.CUMPLE : ComplianceStatus.NO_CUMPLE;
    }

    private double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }
}