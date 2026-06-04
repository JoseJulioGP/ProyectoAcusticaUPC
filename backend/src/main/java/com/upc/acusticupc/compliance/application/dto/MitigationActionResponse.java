package com.upc.acusticupc.compliance.application.dto;

import com.upc.acusticupc.compliance.domain.model.MitigationAction;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Vista de salida de una {@link MitigationAction} (Sprint 7 — RF#11).
 */
public record MitigationActionResponse(
        UUID id,
        String code,
        String title,
        String description,
        String regulationRef,
        Integer priority,
        Double estimatedImpactDb,
        Boolean active,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static MitigationActionResponse from(MitigationAction e) {
        return new MitigationActionResponse(
                e.getId(),
                e.getCode(),
                e.getTitle(),
                e.getDescription(),
                e.getRegulationRef(),
                e.getPriority(),
                e.getEstimatedImpactDb(),
                e.getActive(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }
}
