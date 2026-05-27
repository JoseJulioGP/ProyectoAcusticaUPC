package com.upc.acusticupc.analytics.domain.dto;

import java.util.UUID;

public record ZoneComparisonDTO(
        UUID zoneId,
        String zoneName,
        String sector,
        String subsector,
        Double laeqDiurnoDb,
        Double laeqNocturnoDb,
        double standardDayDb,
        double standardNightDb,
        Double diurnoExcessDb,    // null si laeqDiurnoDb es null
        Double nocturnoExcessDb
) {}
