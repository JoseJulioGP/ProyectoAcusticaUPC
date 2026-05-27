package com.upc.acusticupc.analytics.application.service;

import com.upc.acusticupc.analytics.domain.dto.ZoneComparisonDTO;
import com.upc.acusticupc.analytics.domain.repository.MeasurementStatsRepository;
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
public class ZoneComparisonService {

    private final MeasurementStatsRepository measurementStatsRepository;
    private final ZoneRepository zoneRepository;
    private final NoiseStandardRepository noiseStandardRepository;

    @Transactional(readOnly = true)
    public List<ZoneComparisonDTO> compare(OffsetDateTime from,
                                           OffsetDateTime to,
                                           List<UUID> zoneIds,
                                           Period period) {

        List<Zone> zones = (zoneIds == null || zoneIds.isEmpty())
                ? zoneRepository.findAll()
                : zoneRepository.findAllById(zoneIds);

        // Agrega laeq por (zone, period) una sola vez
        Map<UUID, Map<Period, Double>> laeqMap = new HashMap<>();
        for (Object[] row : measurementStatsRepository.laeqByZoneAndPeriod(from, to, null)) {
            UUID zoneId = (UUID) row[0];
            Period p = Period.valueOf((String) row[1]);
            double laeq = ((Number) row[2]).doubleValue();
            laeqMap.computeIfAbsent(zoneId, k -> new EnumMap<>(Period.class)).put(p, laeq);
        }

        return zones.stream().map(z -> {
            Map<Period, Double> byP = laeqMap.getOrDefault(z.getId(), Map.of());
            Double dia = byP.get(Period.DIURNO);
            Double noche = byP.get(Period.NOCTURNO);

            // Filtra por periodo si se pidio uno solo
            if (period == Period.DIURNO) noche = null;
            if (period == Period.NOCTURNO) dia = null;

            NoiseStandard stdDia = noiseStandardRepository
                    .findBySectorAndSubsectorAndPeriodAndStandardType(
                            z.getSector(), z.getSubsector(), Period.DIURNO, StandardType.AMBIENT)
                    .orElseThrow(() -> new IllegalStateException(
                            "No hay estandar AMBIENT DIURNO para zona " + z.getName()));

            NoiseStandard stdNoche = noiseStandardRepository
                    .findBySectorAndSubsectorAndPeriodAndStandardType(
                            z.getSector(), z.getSubsector(), Period.NOCTURNO, StandardType.AMBIENT)
                    .orElseThrow(() -> new IllegalStateException(
                            "No hay estandar AMBIENT NOCTURNO para zona " + z.getName()));

            Double diaExcess = (dia == null) ? null : round1(dia - stdDia.getMaxDb());
            Double nocheExcess = (noche == null) ? null : round1(noche - stdNoche.getMaxDb());

            return new ZoneComparisonDTO(
                    z.getId(),
                    z.getName(),
                    z.getSector().name(),
                    z.getSubsector().name(),
                    dia != null ? round1(dia) : null,
                    noche != null ? round1(noche) : null,
                    stdDia.getMaxDb(),
                    stdNoche.getMaxDb(),
                    diaExcess,
                    nocheExcess
            );
        }).toList();
    }

    private double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }
}