package com.upc.acusticupc.zones.infrastructure.web.dto;

import com.upc.acusticupc.zones.domain.model.Sector;
import com.upc.acusticupc.zones.domain.model.Subsector;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo de creación/edición de zona. La clasificación normativa
 * (sector/subsector) es obligatoria; las coordenadas de plano son opcionales.
 */
public record ZoneRequest(
        @NotBlank @Size(max = 150) String name,
        @Size(max = 500) String description,
        @Size(max = 100) String building,
        @Size(max = 50) String floor,
        @NotNull Sector sector,
        @NotNull Subsector subsector,
        Boolean active,
        Double mapX,
        Double mapY,
        Double mapWidth,
        Double mapHeight
) {}
