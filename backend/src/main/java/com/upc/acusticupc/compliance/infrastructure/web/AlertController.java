package com.upc.acusticupc.compliance.infrastructure.web;

import com.upc.acusticupc.compliance.application.dto.AlertResponse;
import com.upc.acusticupc.compliance.application.service.AlertService;
import com.upc.acusticupc.compliance.domain.model.AlertSeverity;
import com.upc.acusticupc.ingestion.infrastructure.web.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Drilldown de alertas: listado paginado con filtros opcionales por zona,
 * rango temporal y severidad.
 */
@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public PageResponse<AlertResponse> list(
            @RequestParam(required = false) UUID zoneId,
            @RequestParam(required = false) OffsetDateTime from,
            @RequestParam(required = false) OffsetDateTime to,
            @RequestParam(required = false) AlertSeverity severity,
            @PageableDefault(size = 20, sort = "triggeredAt") Pageable pageable) {
        return PageResponse.from(
                alertService.findAlerts(zoneId, from, to, severity, pageable), r -> r);
    }
}
