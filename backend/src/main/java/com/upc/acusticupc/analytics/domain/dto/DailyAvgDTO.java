package com.upc.acusticupc.analytics.domain.dto;

import java.time.OffsetDateTime;

public record DailyAvgDTO(
        OffsetDateTime day,
        double avgDb,
        long sampleCount
) {}
