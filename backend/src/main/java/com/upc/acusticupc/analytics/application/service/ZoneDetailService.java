package com.upc.acusticupc.analytics.application.service;

import com.upc.acusticupc.analytics.domain.dto.ZoneDetailDTO;
import com.upc.acusticupc.analytics.domain.dto.ZoneStatsDTO;
import com.upc.acusticupc.analytics.domain.repository.AlertStatsRepository;
import com.upc.acusticupc.compliance.application.dto.AlertDTO;
import com.upc.acusticupc.ingestion.application.dto.BatchSummaryDTO;
import com.upc.acusticupc.ingestion.application.mapper.BatchMapper;
import com.upc.acusticupc.sonometry.domain.repository.MeasurementBatchRepository;
import com.upc.acusticupc.zones.domain.model.Zone;
import com.upc.acusticupc.zones.domain.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ZoneDetailService {

    private final ZoneRepository zoneRepository;
    private final ZoneStatsService zoneStatsService;
    private final MeasurementBatchRepository measurementBatchRepository;
    private final AlertStatsRepository alertStatsRepository;
    private final BatchMapper batchMapper;

    @Transactional(readOnly = true)
    public ZoneDetailDTO detail(UUID zoneId, OffsetDateTime from, OffsetDateTime to) {

        Zone zone = zoneRepository.findById(zoneId)
                .orElseThrow(() -> new IllegalArgumentException("Zona no encontrada: " + zoneId));

        // Reutilizamos statsForAllZones y filtramos la zona pedida
        ZoneStatsDTO stats = zoneStatsService.statsForAllZones(from, to).stream()
                .filter(s -> s.zoneId().equals(zoneId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No se pudo construir stats de zona " + zoneId));

        List<BatchSummaryDTO> recentBatches = measurementBatchRepository
                .findTop5ByZoneIdOrderByUploadedAtDesc(zoneId)
                .stream()
                .map(batchMapper::toSummary)
                .toList();

        List<AlertDTO> recentAlerts = alertStatsRepository
                .findTop10ByZoneIdOrderByTriggeredAtDesc(zoneId)
                .stream()
                .map(AlertDTO::from)
                .toList();

        return new ZoneDetailDTO(
                zone.getId(),
                zone.getName(),
                zone.getSector().name(),
                zone.getSubsector().name(),
                zone.getDescription(),
                stats,
                recentBatches,
                recentAlerts
        );
    }
}