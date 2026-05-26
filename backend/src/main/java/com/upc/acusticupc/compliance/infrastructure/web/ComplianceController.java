package com.upc.acusticupc.compliance.infrastructure.web;

import com.upc.acusticupc.compliance.application.dto.AlertDTO;
import com.upc.acusticupc.compliance.application.dto.ComplianceResultDTO;
import com.upc.acusticupc.compliance.application.service.ComplianceEngine;
import com.upc.acusticupc.compliance.domain.repository.AlertRepository;
import com.upc.acusticupc.compliance.domain.repository.ComplianceResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/compliance")
@RequiredArgsConstructor
public class ComplianceController {

    private final ComplianceEngine engine;
    private final ComplianceResultRepository resultRepository;
    private final AlertRepository alertRepository;

    /**
     * Dispara evaluacion de cumplimiento de un batch ya COMPLETED.
     * GET /api/v1/compliance/results/batch/{batchId} para ver el progreso.
     */
    @PostMapping("/evaluate/{batchId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> evaluateBatch(@PathVariable UUID batchId) {
        engine.evaluateBatch(batchId);  // @Async, retorna inmediato
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of(
                "batchId", batchId,
                "message", "Evaluacion de cumplimiento iniciada"
        ));
    }

    /**
     * Resultados de cumplimiento de un batch (1 o 2: uno por period existente).
     */
    @GetMapping("/results/batch/{batchId}")
    @PreAuthorize("hasAnyRole('ADMIN','VIEWER')")
    public List<ComplianceResultDTO> getByBatch(@PathVariable UUID batchId) {
        return resultRepository.findByBatchId(batchId).stream()
                .map(ComplianceResultDTO::from)
                .toList();
    }

    /**
     * Resultados por zona en un rango de fechas. Paginado.
     */
    @GetMapping("/results")
    @PreAuthorize("hasAnyRole('ADMIN','VIEWER')")
    public Page<ComplianceResultDTO> listResults(
            @RequestParam UUID zoneId,
            @RequestParam OffsetDateTime from,
            @RequestParam OffsetDateTime to,
            Pageable pageable) {
        return resultRepository
                .findByZoneIdAndEvaluatedAtBetween(zoneId, from, to, pageable)
                .map(ComplianceResultDTO::from);
    }

    /**
     * Alertas (filtros opcionales). Paginado.
     */
    @GetMapping("/alerts")
    @PreAuthorize("hasAnyRole('ADMIN','VIEWER')")
    public Page<AlertDTO> listAlerts(
            @RequestParam(required = false) UUID zoneId,
            @RequestParam OffsetDateTime from,
            @RequestParam OffsetDateTime to,
            Pageable pageable) {
        var page = (zoneId != null)
                ? alertRepository.findByZoneIdAndTriggeredAtBetween(zoneId, from, to, pageable)
                : alertRepository.findByTriggeredAtBetween(from, to, pageable);
        return page.map(AlertDTO::from);
    }
}
