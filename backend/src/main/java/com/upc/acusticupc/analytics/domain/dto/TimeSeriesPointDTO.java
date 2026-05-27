package com.upc.acusticupc.analytics.domain.dto;

import com.upc.acusticupc.sonometry.domain.model.Period;

import java.time.OffsetDateTime;
import java.util.UUID;

public record TimeSeriesPointDTO(
        OffsetDateTime bucket,
        UUID zoneId,
        String zoneName,
        double laeqDb,
        long sampleCount,
        Period period
) {}
