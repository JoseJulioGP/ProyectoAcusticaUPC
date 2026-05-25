package com.upc.acusticupc.compliance.domain.repository;

import com.upc.acusticupc.compliance.domain.model.ComplianceResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface ComplianceResultRepository extends JpaRepository<ComplianceResult, UUID> {

    List<ComplianceResult> findByBatchId(UUID batchId);

    Page<ComplianceResult> findByZoneIdAndEvaluatedAtBetween(
        UUID zoneId, OffsetDateTime from, OffsetDateTime to, Pageable pageable);
}
