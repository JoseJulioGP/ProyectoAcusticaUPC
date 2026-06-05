package com.upc.acusticupc.ingestion.infrastructure.web.dto;

import java.util.UUID;

/**
 * Cuerpo del PATCH para mover un batch a una carpeta.
 * folderId null = quitar de cualquier carpeta (sin clasificar).
 */
public record BatchFolderRequest(
        UUID folderId
) {}
