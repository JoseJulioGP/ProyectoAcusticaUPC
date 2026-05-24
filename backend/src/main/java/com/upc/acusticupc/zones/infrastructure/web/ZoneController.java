package com.upc.acusticupc.zones.infrastructure.web;

import com.upc.acusticupc.zones.domain.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Endpoint mínimo para que el frontend pueble dropdowns con las zonas sembradas.
 * En Sprint 3+ se ampliará con sector/subsector y soporte CRUD.
 */
@RestController
@RequestMapping("/api/v1/zones")
@RequiredArgsConstructor
public class ZoneController {

    private final ZoneRepository zoneRepository;

    record ZoneDTO(UUID id, String name) {}

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<ZoneDTO> list() {
        return zoneRepository.findAll().stream()
                .map(z -> new ZoneDTO(z.getId(), z.getName()))
                .toList();
    }
}