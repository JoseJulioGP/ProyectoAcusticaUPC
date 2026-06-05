package com.upc.acusticupc.zones.infrastructure.web.dto;

import com.upc.acusticupc.zones.domain.model.Sector;
import com.upc.acusticupc.zones.domain.model.Subsector;
import com.upc.acusticupc.zones.domain.model.Zone;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ZoneResponse(
        UUID id,
        String name,
        String description,
        String building,
        String floor,
        Sector sector,
        Subsector subsector,
        boolean active,
        Double mapX,
        Double mapY,
        Double mapWidth,
        Double mapHeight,
        OffsetDateTime createdAt
) {
    public static ZoneResponse from(Zone z) {
        return new ZoneResponse(
                z.getId(),
                z.getName(),
                z.getDescription(),
                z.getBuilding(),
                z.getFloor(),
                z.getSector(),
                z.getSubsector(),
                z.isActive(),
                z.getMapX(),
                z.getMapY(),
                z.getMapWidth(),
                z.getMapHeight(),
                z.getCreatedAt()
        );
    }
}
