package com.upc.acusticupc.analytics.application.service;

import com.upc.acusticupc.analytics.domain.dto.HeatmapDTO;
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
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class HeatmapService {

    private final MeasurementStatsRepository measurementStatsRepository;
    private final ZoneRepository zoneRepository;
    private final NoiseStandardRepository noiseStandardRepository;

    @Transactional(readOnly = true)
    public HeatmapDTO buildHeatmap(OffsetDateTime from, OffsetDateTime to, Period period) {

        List<Zone> zones = zoneRepository.findAll().stream()
                .sorted(Comparator.comparing(Zone::getName))
                .toList();

        int Z = zones.size();
        int H = 24;

        double[][] laeq = new double[Z][H];
        double[][] excess = new double[Z][H];

        // Inicializa con -1 (sin datos)
        for (int z = 0; z < Z; z++) {
            Arrays.fill(laeq[z], -1.0);
            Arrays.fill(excess[z], -1.0);
        }

        // Mapeo zoneId -> indice de fila
        Map<UUID, Integer> zoneIndex = new HashMap<>();
        for (int i = 0; i < zones.size(); i++) zoneIndex.put(zones.get(i).getId(), i);

        // Estandares por zona: [diurno, nocturno]
        Map<UUID, double[]> stdByZone = new HashMap<>();
        for (Zone z : zones) {
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

            stdByZone.put(z.getId(), new double[]{stdDia.getMaxDb(), stdNoche.getMaxDb()});
        }

        String periodStr = (period == null) ? null : period.name();
        List<Object[]> rows = measurementStatsRepository.laeqByZoneAndHour(from, to, periodStr);

        for (Object[] r : rows) {
            UUID zId = (UUID) r[0];
            int hour = ((Number) r[1]).intValue();
            double l = ((Number) r[2]).doubleValue();
            Integer idx = zoneIndex.get(zId);
            if (idx == null) continue;

            laeq[idx][hour] = round1(l);

            // Determinar estandar segun la hora (diurno 7-21, nocturno 21-7)
            double[] std = stdByZone.get(zId);
            double applicableStd = (hour >= 7 && hour < 21) ? std[0] : std[1];
            excess[idx][hour] = round1(l - applicableStd);
        }

        return new HeatmapDTO(
                zones.stream().map(Zone::getName).toList(),
                IntStream.range(0, H).boxed().toList(),
                laeq,
                excess
        );
    }

    private double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }
}