package com.upc.acusticupc.ingestion.application.dto;

import com.upc.acusticupc.sonometry.domain.model.BatchStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Vista resumida de un batch para listados y polling de estado.
 */
public record BatchSummaryDTO(
        UUID id,
        String fileName,
        String zoneName,
        BatchStatus status,
        Integer totalRows,
        Integer validRows,
        Integer rejectedRows,
        String errorMessage,
        OffsetDateTime uploadedAt,
        OffsetDateTime processedAt,
        String observation
) {}
