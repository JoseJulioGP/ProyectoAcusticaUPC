package com.upc.acusticupc.compliance.application.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo de creación/edición de {@code MitigationAction} (Sprint 7 — RF#11).
 *
 * <p>Usado en {@code POST /api/v1/mitigations} y {@code PUT /api/v1/mitigations/{id}}.
 * El campo {@code active} es opcional: si llega {@code null}, en {@code create}
 * el servicio asume {@code true} (alta nueva activa) y en {@code update} no se toca.</p>
 */
public record MitigationActionRequest(
        @NotBlank
        @Size(max = 16, message = "code máximo 16 caracteres")
        String code,

        @NotBlank
        @Size(max = 160, message = "title máximo 160 caracteres")
        String title,

        @NotBlank
        String description,

        @Size(max = 120, message = "regulationRef máximo 120 caracteres")
        String regulationRef,

        @NotNull
        @Min(value = 1, message = "priority entre 1 (alta) y 5 (baja)")
        @Max(value = 5, message = "priority entre 1 (alta) y 5 (baja)")
        Integer priority,

        Double estimatedImpactDb,

        Boolean active
) {}
