package com.upc.acusticupc.ingestion.infrastructure.web.dto;

import jakarta.validation.constraints.Size;

/**
 * Cuerpo del PATCH de observación de un batch.
 * La observación es libre y opcional (puede venir vacía o null para limpiarla).
 */
public record BatchObservationRequest(
        @Size(max = 4000) String observation
) {}
