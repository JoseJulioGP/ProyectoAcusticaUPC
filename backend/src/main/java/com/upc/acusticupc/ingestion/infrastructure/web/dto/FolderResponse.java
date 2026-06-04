package com.upc.acusticupc.ingestion.infrastructure.web.dto;

import com.upc.acusticupc.ingestion.domain.model.IngestionFolder;

import java.time.OffsetDateTime;
import java.util.UUID;

public record FolderResponse(
        UUID id,
        String name,
        UUID parentId,
        OffsetDateTime createdAt
) {
    public static FolderResponse from(IngestionFolder f) {
        return new FolderResponse(
                f.getId(),
                f.getName(),
                f.getParent() != null ? f.getParent().getId() : null,
                f.getCreatedAt()
        );
    }
}
