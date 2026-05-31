package com.upc.acusticupc.sonometry.domain.repository;

import com.upc.acusticupc.sonometry.domain.model.Measurement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface MeasurementRepository extends JpaRepository<Measurement, UUID> {

    List<Measurement> findByZoneIdAndMeasuredAtBetween(
        UUID zoneId, OffsetDateTime from, OffsetDateTime to);

    /** Rango inclusivo-start / exclusivo-end. Úsalo junto a resolveRange(). */
    @Query("""
        select m from Measurement m
        where (:zoneId is null or m.zone.id = :zoneId)
          and m.measuredAt >= :start and m.measuredAt < :end
        """)
    List<Measurement> findInRange(@Param("zoneId") UUID zoneId,
                                  @Param("start") OffsetDateTime start,
                                  @Param("end") OffsetDateTime end);

    long countByBatchId(UUID batchId);
    List<Measurement> findByBatchId(UUID batchId);

    Page<Measurement> findByBatchId(UUID batchId, Pageable pageable);

    @Modifying
    @Query("delete from Measurement m where m.batch.id = :batchId")
    void deleteByBatchId(@Param("batchId") UUID batchId);

    @Query("select distinct m.zone.id from Measurement m where m.batch.id = :batchId")
    List<UUID> findZoneIdsByBatchId(@Param("batchId") UUID batchId);
}