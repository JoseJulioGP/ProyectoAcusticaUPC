package com.upc.acusticupc.compliance.domain.repository;

import com.upc.acusticupc.compliance.domain.model.Alert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface AlertRepository extends JpaRepository<Alert, UUID> {

    Page<Alert> findByZoneIdAndTriggeredAtBetween(
        UUID zoneId, OffsetDateTime from, OffsetDateTime to, Pageable pageable);

    Page<Alert> findByTriggeredAtBetween(
        OffsetDateTime from, OffsetDateTime to, Pageable pageable);

    /**
     * Drilldown con filtros todos opcionales (zona, rango temporal, severidad).
     *
     * <p>Sprint 8 fix: nativa con CAST explícito. Postgres no puede inferir el
     * tipo de un parámetro {@code null} sin tipo, lo que rompía /alerts y el
     * buildNoConformidades del Excel/PDF al pasar zoneId/severity en null. El
     * llamante pasa {@code severity.name()} o {@code null}.</p>
     */
    @Query(value = """
        SELECT * FROM alerts a
        WHERE (CAST(:zoneId AS uuid) IS NULL OR a.zone_id = CAST(:zoneId AS uuid))
          AND (CAST(:from AS timestamptz) IS NULL OR a.triggered_at >= CAST(:from AS timestamptz))
          AND (CAST(:to AS timestamptz) IS NULL OR a.triggered_at <= CAST(:to AS timestamptz))
          AND (CAST(:severity AS text) IS NULL OR a.severity = CAST(:severity AS text))
        """,
        countQuery = """
        SELECT count(*) FROM alerts a
        WHERE (CAST(:zoneId AS uuid) IS NULL OR a.zone_id = CAST(:zoneId AS uuid))
          AND (CAST(:from AS timestamptz) IS NULL OR a.triggered_at >= CAST(:from AS timestamptz))
          AND (CAST(:to AS timestamptz) IS NULL OR a.triggered_at <= CAST(:to AS timestamptz))
          AND (CAST(:severity AS text) IS NULL OR a.severity = CAST(:severity AS text))
        """,
        nativeQuery = true)
    Page<Alert> search(@Param("zoneId") UUID zoneId,
                       @Param("from") OffsetDateTime from,
                       @Param("to") OffsetDateTime to,
                       @Param("severity") String severity,
                       Pageable pageable);

    List<Alert> findByBatchId(UUID batchId);

    @Modifying
    @Query("delete from Alert a where a.batch.id = :batchId")
    void deleteByBatchId(@Param("batchId") UUID batchId);
}
