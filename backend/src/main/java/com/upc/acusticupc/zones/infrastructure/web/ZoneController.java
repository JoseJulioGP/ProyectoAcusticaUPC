package com.upc.acusticupc.zones.infrastructure.web;

import com.upc.acusticupc.zones.application.ZoneService;
import com.upc.acusticupc.zones.infrastructure.web.dto.ZoneRequest;
import com.upc.acusticupc.zones.infrastructure.web.dto.ZoneResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * CRUD de zonas. La stakeholder puede crear/editar/eliminar bloques y zonas
 * con libertad. Soft delete por defecto; hard delete bloqueado si hay batches.
 */
@RestController
@RequestMapping("/api/v1/zones")
@RequiredArgsConstructor
public class ZoneController {

    private final ZoneService zoneService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<ZoneResponse> list() {
        return zoneService.list();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ZoneResponse get(@PathVariable UUID id) {
        return zoneService.get(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    public ResponseEntity<ZoneResponse> create(@Valid @RequestBody ZoneRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(zoneService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    public ZoneResponse update(@PathVariable UUID id, @Valid @RequestBody ZoneRequest request) {
        return zoneService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id,
                                       @RequestParam(defaultValue = "false") boolean hard) {
        zoneService.delete(id, hard);
        return ResponseEntity.noContent().build();
    }
}
