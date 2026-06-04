package com.upc.acusticupc.analytics.application.service;

import com.upc.acusticupc.analytics.domain.dto.BeforeAfterDTO;
import com.upc.acusticupc.analytics.domain.dto.DailyAvgDTO;
import com.upc.acusticupc.analytics.domain.dto.WeekdayStatDTO;
import com.upc.acusticupc.analytics.domain.repository.MeasurementStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Analítica de tendencias: promedio por día de la semana y comparación
 * antes/después de una fecha pivote.
 */
@Service
@RequiredArgsConstructor
public class TrendAnalyticsService {

    private final MeasurementStatsRepository statsRepository;

    @Transactional(readOnly = true)
    public List<WeekdayStatDTO> weekday(UUID zoneId, int year, int isoWeekFrom, int isoWeekTo) {
        List<Object[]> rows = statsRepository.avgByWeekday(zoneId, year, isoWeekFrom, isoWeekTo);
        List<WeekdayStatDTO> result = new ArrayList<>(rows.size());
        for (Object[] r : rows) {
            int dow = ((Number) r[0]).intValue();
            double avg = ((Number) r[1]).doubleValue();
            long count = ((Number) r[2]).longValue();
            result.add(new WeekdayStatDTO(dow, round1(avg), count));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public BeforeAfterDTO beforeAfter(UUID zoneId, OffsetDateTime pivot) {
        List<Object[]> rows = statsRepository.dailyAvgBeforeAfter(zoneId, pivot);
        List<DailyAvgDTO> before = new ArrayList<>();
        List<DailyAvgDTO> after = new ArrayList<>();
        for (Object[] r : rows) {
            OffsetDateTime day = toColombiaOffset(r[0]);
            double avg = ((Number) r[1]).doubleValue();
            long count = ((Number) r[2]).longValue();
            boolean isBefore = (Boolean) r[3];
            DailyAvgDTO point = new DailyAvgDTO(day, round1(avg), count);
            (isBefore ? before : after).add(point);
        }
        return new BeforeAfterDTO(before, after);
    }

    private double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }

    private static OffsetDateTime toColombiaOffset(Object raw) {
        ZoneOffset co = ZoneOffset.of("-05:00");
        return switch (raw) {
            case Instant ins -> ins.atOffset(co);
            case Timestamp ts -> ts.toInstant().atOffset(co);
            case OffsetDateTime odt -> odt.withOffsetSameInstant(co);
            case null -> throw new IllegalStateException("día nulo en serie before/after");
            default -> OffsetDateTime.parse(raw.toString());
        };
    }
}
