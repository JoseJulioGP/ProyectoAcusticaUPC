package com.upc.acusticupc.compliance.domain.repository;

import com.upc.acusticupc.compliance.domain.model.ComplianceResult;
import com.upc.acusticupc.compliance.domain.model.ComplianceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface ComplianceResultRepository extends JpaRepository<ComplianceResult, UUID> {

    List<ComplianceResult> findByBatchId(UUID batchId);

    Page<ComplianceResult> findByZoneIdAndEvaluatedAtBetween(
        UUID zoneId, OffsetDateTime from, OffsetDateTime to, Pageable pageable);

    /** Rango inclusivo (legacy). Mantenido para compatibilidad con tests unitarios. */
    long countByEvaluatedAtBetween(OffsetDateTime from, OffsetDateTime to);

    /** Rango inclusivo (legacy). */
    long countByEvaluatedAtBetweenAndStatus(OffsetDateTime from, OffsetDateTime to,
                                            ComplianceStatus status);

    /** Rango exclusivo en el extremo superior — preferir sobre countByEvaluatedAtBetween. */
    @Query("""
        select count(r) from ComplianceResult r
        where r.evaluatedAt >= :start and r.evaluatedAt < :end
        """)
    long countInRange(@Param("start") OffsetDateTime start,
                      @Param("end") OffsetDateTime end);

    /** Rango exclusivo filtrado por estado. */
    @Query("""
        select count(r) from ComplianceResult r
        where r.evaluatedAt >= :start and r.evaluatedAt < :end
          and r.status = :status
        """)
    long countInRangeByStatus(@Param("start") OffsetDateTime start,
                               @Param("end") OffsetDateTime end,
                               @Param("status") ComplianceStatus status);

    @Modifying
    @Query("delete from ComplianceResult r where r.batch.id = :batchId")
    void deleteByBatchId(@Param("batchId") UUID batchId);
}
