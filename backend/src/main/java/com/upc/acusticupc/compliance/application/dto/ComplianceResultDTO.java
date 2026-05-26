package com.upc.acusticupc.compliance.application.dto;

import com.upc.acusticupc.compliance.domain.model.ComplianceResult;
import com.upc.acusticupc.compliance.domain.model.ComplianceStatus;
import com.upc.acusticupc.sonometry.domain.model.Period;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ComplianceResultDTO(
        UUID id,
        UUID zoneId,
        String zoneName,
        UUID batchId,
        String batchFileName,
        Period period,
        Integer measurementCount,
        BigDecimal laeqDb,
        BigDecimal l90Db,
        BigDecimal minDb,
        BigDecimal maxDb,
        BigDecimal standardDb,
        BigDecimal excessDb,
        ComplianceStatus status,
        OffsetDateTime evaluatedFrom,
        OffsetDateTime evaluatedTo,
        OffsetDateTime evaluatedAt,
        String standardType
) {
    public static ComplianceResultDTO from(ComplianceResult r) {
        BigDecimal excess = r.getLaeqDb().subtract(r.getStandardDb());
        return new ComplianceResultDTO(
                r.getId(),
                r.getZone().getId(),
                r.getZone().getName(),
                r.getBatch() != null ? r.getBatch().getId() : null,
                r.getBatch() != null ? r.getBatch().getFileName() : null,
                r.getPeriod(),
                r.getMeasurementCount(),
                r.getLaeqDb(),
                r.getL90Db(),
                r.getMinDb(),
                r.getMaxDb(),
                r.getStandardDb(),
                excess,
                r.getStatus(),
                r.getEvaluatedFrom(),
                r.getEvaluatedTo(),
                r.getEvaluatedAt(),
                r.getStandardType()
        );
    }
}
