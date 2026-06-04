package com.upc.acusticupc.compliance.infrastructure.web;

import com.upc.acusticupc.compliance.application.dto.MitigationActionRequest;
import com.upc.acusticupc.compliance.application.dto.MitigationActionResponse;
import com.upc.acusticupc.compliance.application.service.MitigationActionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Endpoints del catálogo de acciones de mitigación (Sprint 7 — RF#11).
 *
 * <ul>
 *   <li>{@code GET /api/v1/mitigations}: lista las acciones activas (cualquier autenticado).</li>
 *   <li>{@code GET /api/v1/mitigations/suggest?excessDb=X}: sugerencias por exceso (cualquier autenticado).</li>
 *   <li>{@code POST /api/v1/mitigations}: alta (ADMIN o ANALYST).</li>
 *   <li>{@code PUT /api/v1/mitigations/{id}}: edición (ADMIN o ANALYST).</li>
 *   <li>{@code DELETE /api/v1/mitigations/{id}}: desactivación lógica (solo ADMIN).</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/mitigations")
@RequiredArgsConstructor
public class MitigationController {

    private final MitigationActionService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<MitigationActionResponse> list() {
        return service.listActive().stream()
                .map(MitigationActionResponse::from)
                .toList();
    }

    @GetMapping("/suggest")
    @PreAuthorize("isAuthenticated()")
    public List<MitigationActionResponse> suggest(@RequestParam double excessDb) {
        return service.suggest(excessDb).stream()
                .map(MitigationActionResponse::from)
                .toList();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    public ResponseEntity<MitigationActionResponse> create(@Valid @RequestBody MitigationActionRequest req) {
        MitigationActionResponse created = MitigationActionResponse.from(service.create(req));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    public MitigationActionResponse update(@PathVariable UUID id,
                                           @Valid @RequestBody MitigationActionRequest req) {
        return MitigationActionResponse.from(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        service.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
