package com.upc.acusticupc.analytics.infrastructure.web;

import com.upc.acusticupc.analytics.application.service.*;
import com.upc.acusticupc.analytics.domain.dto.*;
import com.upc.acusticupc.sonometry.domain.model.Period;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','VIEWER')")
public class AnalyticsController {

    private final KpiService kpiService;
    private final ZoneStatsService zoneStatsService;
    private final AlertStatsService alertStatsService;
    private final ZoneDetailService zoneDetailService;
    private final TimeSeriesService timeSeriesService;
    private final HeatmapService heatmapService;
    private final ZoneComparisonService zoneComparisonService;

    @GetMapping("/kpis")
    @Timed("analytics.kpis")
    public ResponseEntity<DashboardKpisDTO> kpis(
            @RequestParam(required = false) OffsetDateTime from,
            @RequestParam(required = false) OffsetDateTime to,
            @RequestParam(required = false) UUID zoneId,
            @RequestParam(required = false) Period period
    ) {
        var range = defaultRange(from, to);
        return ResponseEntity.ok(kpiService.computeKpis(range.from, range.to, zoneId, period));
    }

    @GetMapping("/zones/stats")
    public ResponseEntity<List<ZoneStatsDTO>> zonesStats(
            @RequestParam(required = false) OffsetDateTime from,
            @RequestParam(required = false) OffsetDateTime to
    ) {
        var range = defaultRange(from, to);
        return ResponseEntity.ok(zoneStatsService.statsForAllZones(range.from, range.to));
    }

    @GetMapping("/zones/{zoneId}")
    public ResponseEntity<ZoneDetailDTO> zoneDetail(
            @PathVariable UUID zoneId,
            @RequestParam(required = false) OffsetDateTime from,
            @RequestParam(required = false) OffsetDateTime to
    ) {
        var range = defaultRange(from, to);
        return ResponseEntity.ok(zoneDetailService.detail(zoneId, range.from, range.to));
    }

    @GetMapping("/alerts/summary")
    public ResponseEntity<AlertsSummaryDTO> alertsSummary(
            @RequestParam(required = false) OffsetDateTime from,
            @RequestParam(required = false) OffsetDateTime to
    ) {
        var range = defaultRange(from, to);
        return ResponseEntity.ok(alertStatsService.summary(range.from, range.to));
    }

    @GetMapping("/timeseries")
    @Timed("analytics.timeseries")
    public ResponseEntity<List<TimeSeriesPointDTO>> timeSeries(
            @RequestParam(required = false) OffsetDateTime from,
            @RequestParam(required = false) OffsetDateTime to,
            @RequestParam(required = false) UUID zoneId,
            @RequestParam(required = false) Period period,
            @RequestParam(defaultValue = "HOUR") Granularity granularity
    ) {
        var range = defaultRange(from, to);
        return ResponseEntity.ok(
                timeSeriesService.series(range.from(), range.to(), zoneId, period, granularity)
        );
    }

    @GetMapping("/heatmap")
    @Timed("analytics.heatmap")
    public ResponseEntity<HeatmapDTO> heatmap(
            @RequestParam(required = false) OffsetDateTime from,
            @RequestParam(required = false) OffsetDateTime to,
            @RequestParam(required = false) Period period
    ) {
        var range = defaultRange(from, to);
        return ResponseEntity.ok(heatmapService.buildHeatmap(range.from(), range.to(), period));
    }

    // --- default range helper ---
    private record Range(OffsetDateTime from, OffsetDateTime to) {}

    private Range defaultRange(OffsetDateTime from, OffsetDateTime to) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.of("-05:00")); // Colombia
        OffsetDateTime t = (to != null) ? to : now;
        OffsetDateTime f = (from != null) ? from : t.minusDays(30);
        return new Range(f, t);
    }
}
