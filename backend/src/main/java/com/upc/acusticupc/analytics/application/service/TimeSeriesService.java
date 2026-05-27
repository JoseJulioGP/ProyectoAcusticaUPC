package com.upc.acusticupc.analytics.application.service;

import com.upc.acusticupc.analytics.application.util.TimeBucketer;
import com.upc.acusticupc.analytics.domain.dto.Granularity;
import com.upc.acusticupc.analytics.domain.dto.TimeSeriesPointDTO;
import com.upc.acusticupc.analytics.domain.repository.MeasurementStatsRepository;
import com.upc.acusticupc.sonometry.domain.model.Period;
import com.upc.acusticupc.zones.domain.model.Zone;
import com.upc.acusticupc.zones.domain.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TimeSeriesService {

    private final MeasurementStatsRepository measurementStatsRepository;
    private final ZoneRepository zoneRepository;

    @Transactional(readOnly = true)
    public List<TimeSeriesPointDTO> series(OffsetDateTime from,
                                           OffsetDateTime to,
                                           UUID zoneId,
                                           Period period,
                                           Granularity granularity) {

        // Cache de nombres de zona (no traer 1 zona por punto)
        Map<UUID, String> zoneNames = new HashMap<>();
        for (Zone z : zoneRepository.findAll()) {
            zoneNames.put(z.getId(), z.getName());
        }

        String unit = TimeBucketer.dateTruncUnit(granularity);
        String periodStr = (period == null) ? null : period.name();

        List<Object[]> rows = measurementStatsRepository
                .laeqTimeSeries(unit, from, to, zoneId, periodStr);

        List<TimeSeriesPointDTO> result = new ArrayList<>(rows.size());
        for (Object[] r : rows) {
            OffsetDateTime bucket = toColombiaOffset(r[0]);
            UUID zId = (UUID) r[1];
            double laeq = ((Number) r[2]).doubleValue();
            long count = ((Number) r[3]).longValue();
            Period p = Period.valueOf((String) r[4]);

            result.add(new TimeSeriesPointDTO(
                    bucket,
                    zId,
                    zoneNames.getOrDefault(zId, "Zona desconocida"),
                    round1(laeq),
                    count,
                    p
            ));
        }
        return result;
    }

    private double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }

    /**
     * El bucket de date_trunc sobre una columna timestamptz puede llegar como
     * Instant (driver pg moderno), Timestamp (legacy) u OffsetDateTime. Se normaliza
     * todo al offset de Colombia.
     */
    private static OffsetDateTime toColombiaOffset(Object raw) {
        ZoneOffset co = ZoneOffset.of("-05:00");
        return switch (raw) {
            case Instant ins -> ins.atOffset(co);
            case Timestamp ts -> ts.toInstant().atOffset(co);
            case OffsetDateTime odt -> odt.withOffsetSameInstant(co);
            case null -> throw new IllegalStateException("bucket nulo en serie temporal");
            default -> OffsetDateTime.parse(raw.toString());
        };
    }
}
