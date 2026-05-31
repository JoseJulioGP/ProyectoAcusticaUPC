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

    List<Alert> findByBatchId(UUID batchId);

    @Modifying
    @Query("delete from Alert a where a.batch.id = :batchId")
    void deleteByBatchId(@Param("batchId") UUID batchId);
}
