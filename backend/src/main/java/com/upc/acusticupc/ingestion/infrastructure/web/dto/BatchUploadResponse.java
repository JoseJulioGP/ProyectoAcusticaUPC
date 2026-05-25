package com.upc.acusticupc.ingestion.infrastructure.web.dto;

import com.upc.acusticupc.sonometry.domain.model.BatchStatus;

import java.util.UUID;

/**
 * Devuelto inmediatamente tras un upload exitoso, antes de que termine el procesamiento.
 * El batch arranca en PENDING y luego pasa a PROCESSING → COMPLETED/FAILED.
 */
public record BatchUploadResponse(
        UUID batchId,
        BatchStatus status,
        String message
) {
    public static BatchUploadResponse pending(UUID id) {
        return new BatchUploadResponse(
                id,
                BatchStatus.PENDING,
                "Archivo recibido. Procesamiento en curso."
        );
    }
}