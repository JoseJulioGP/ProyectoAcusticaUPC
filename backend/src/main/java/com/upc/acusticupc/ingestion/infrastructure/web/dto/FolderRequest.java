package com.upc.acusticupc.ingestion.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Cuerpo de creación/edición de carpeta. parentId opcional (null = carpeta raíz).
 */
public record FolderRequest(
        @NotBlank @Size(max = 120) String name,
        UUID parentId
) {}
