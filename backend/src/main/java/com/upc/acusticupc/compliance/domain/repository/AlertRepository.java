package com.upc.acusticupc.compliance.domain.repository;

import com.upc.acusticupc.compliance.domain.model.Alert;
import com.upc.acusticupc.compliance.domain.model.AlertSeverity;
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

    /** Drilldown con filtros todos opcionales (zona, rango temporal, severidad). */
    @Query("""
        SELECT a FROM Alert a
        WHERE (:zoneId IS NULL OR a.zone.id = :zoneId)
          AND (:from IS NULL OR a.triggeredAt >= :from)
          AND (:to IS NULL OR a.triggeredAt <= :to)
          AND (:severity IS NULL OR a.severity = :severity)
        """)
    Page<Alert> search(@Param("zoneId") UUID zoneId,
                       @Param("from") OffsetDateTime from,
                       @Param("to") OffsetDateTime to,
                       @Param("severity") AlertSeverity severity,
                       Pageable pageable);

    List<Alert> findByBatchId(UUID batchId);

    @Modifying
    @Query("delete from Alert a where a.batch.id = :batchId")
    void deleteByBatchId(@Param("batchId") UUID batchId);
}
