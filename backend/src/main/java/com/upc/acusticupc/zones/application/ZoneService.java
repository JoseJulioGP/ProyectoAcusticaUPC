package com.upc.acusticupc.zones.application;

import com.upc.acusticupc.shared.exception.ResourceNotFoundException;
import com.upc.acusticupc.sonometry.domain.repository.MeasurementBatchRepository;
import com.upc.acusticupc.zones.domain.model.Zone;
import com.upc.acusticupc.zones.domain.repository.ZoneRepository;
import com.upc.acusticupc.zones.infrastructure.web.dto.ZoneRequest;
import com.upc.acusticupc.zones.infrastructure.web.dto.ZoneResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ZoneService {

    private final ZoneRepository zoneRepository;
    private final MeasurementBatchRepository batchRepository;

    @Transactional(readOnly = true)
    public List<ZoneResponse> list() {
        return zoneRepository.findAll().stream().map(ZoneResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ZoneResponse get(UUID id) {
        return ZoneResponse.from(findOrThrow(id));
    }

    @Transactional
    public ZoneResponse create(ZoneRequest req) {
        Zone zone = Zone.builder()
                .name(req.name())
                .description(req.description())
                .building(req.building())
                .floor(req.floor())
                .sector(req.sector())
                .subsector(req.subsector())
                .active(req.active() == null || req.active())
                .mapX(req.mapX())
                .mapY(req.mapY())
                .mapWidth(req.mapWidth())
                .mapHeight(req.mapHeight())
                .build();
        zone = zoneRepository.save(zone);
        log.info("Zona creada: {} (id={})", zone.getName(), zone.getId());
        return ZoneResponse.from(zone);
    }

    @Transactional
    public ZoneResponse update(UUID id, ZoneRequest req) {
        Zone zone = findOrThrow(id);
        zone.setName(req.name());
        zone.setDescription(req.description());
        zone.setBuilding(req.building());
        zone.setFloor(req.floor());
        zone.setSector(req.sector());
        zone.setSubsector(req.subsector());
        if (req.active() != null) zone.setActive(req.active());
        zone.setMapX(req.mapX());
        zone.setMapY(req.mapY());
        zone.setMapWidth(req.mapWidth());
        zone.setMapHeight(req.mapHeight());
        return ZoneResponse.from(zoneRepository.save(zone));
    }

    /**
     * hard=false (defecto): soft delete (active=false).
     * hard=true: borrado real, pero rechaza con ZONE_IN_USE (409) si hay batches asociados.
     */
    @Transactional
    public void delete(UUID id, boolean hard) {
        Zone zone = findOrThrow(id);
        if (!hard) {
            zone.setActive(false);
            zoneRepository.save(zone);
            log.info("Zona {} desactivada (soft delete)", id);
            return;
        }
        if (batchRepository.existsByZoneId(id)) {
            throw new IllegalStateException(
                    "ZONE_IN_USE: la zona tiene batches asociados; reasígnalos o usa soft delete");
        }
        zoneRepository.delete(zone);
        log.info("Zona {} eliminada (hard delete)", id);
    }

    private Zone findOrThrow(UUID id) {
        return zoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zone", id));
    }
}
