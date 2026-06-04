package com.upc.acusticupc.ingestion.domain.repository;

import com.upc.acusticupc.ingestion.domain.model.IngestionFolder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IngestionFolderRepository extends JpaRepository<IngestionFolder, UUID> {

    /** Para bloquear el borrado de carpetas con subcarpetas (FOLDER_HAS_CHILDREN). */
    boolean existsByParentId(UUID parentId);
}
