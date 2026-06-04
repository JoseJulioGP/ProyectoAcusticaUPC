package com.upc.acusticupc.sonometry.domain.repository;

import com.upc.acusticupc.sonometry.domain.model.BatchStatus;
import com.upc.acusticupc.sonometry.domain.model.MeasurementBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MeasurementBatchRepository extends JpaRepository<MeasurementBatch, UUID> {

    List<MeasurementBatch> findByStatusOrderByUploadedAtDesc(BatchStatus status);
    List<MeasurementBatch> findTop5ByZoneIdOrderByUploadedAtDesc(UUID zoneId);

    /** Para bloquear el borrado de carpetas con batches asociados (FOLDER_IN_USE). */
    boolean existsByFolderId(UUID folderId);

    /** Para bloquear el hard delete de zonas con batches asociados (ZONE_IN_USE). */
    boolean existsByZoneId(UUID zoneId);

    /** Batches con observación, para la hoja "Observaciones" del export Excel. */
    List<MeasurementBatch> findByObservationIsNotNull();
}