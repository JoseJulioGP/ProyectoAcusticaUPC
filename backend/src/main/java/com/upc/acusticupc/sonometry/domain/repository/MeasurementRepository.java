package com.upc.acusticupc.sonometry.domain.repository;

import com.upc.acusticupc.sonometry.domain.model.Measurement;
import org.springframework.data.jpa.repository.JpaRepository;
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

    long countByBatchId(UUID batchId);

    Page<Measurement> findByBatchId(UUID batchId, Pageable pageable);
}