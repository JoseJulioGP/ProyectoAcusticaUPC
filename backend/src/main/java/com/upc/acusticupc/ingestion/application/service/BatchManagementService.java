package com.upc.acusticupc.ingestion.application.service;

import com.upc.acusticupc.shared.exception.ResourceNotFoundException;
import com.upc.acusticupc.shared.util.DateRangeUtil;
import com.upc.acusticupc.sonometry.domain.model.BatchStatus;
import com.upc.acusticupc.sonometry.domain.model.MeasurementBatch;
import com.upc.acusticupc.sonometry.domain.repository.MeasurementBatchRepository;
import com.upc.acusticupc.sonometry.domain.repository.MeasurementRepository;
import com.upc.acusticupc.compliance.domain.repository.AlertRepository;
import com.upc.acusticupc.compliance.domain.repository.ComplianceResultRepository;
import com.upc.acusticupc.analytics.application.service.KpiCacheInvalidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BatchManagementService {

    private final MeasurementBatchRepository batchRepository;
    private final MeasurementRepository measurementRepository;
    private final AlertRepository alertRepository;
    private final ComplianceResultRepository complianceResultRepository;
    private final KpiCacheInvalidator kpiCacheInvalidator;

    @Transactional
    public MeasurementBatch retry(UUID id) {
        MeasurementBatch b = batchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Batch", id));
        if (b.getStatus() != BatchStatus.PROCESSING && b.getStatus() != BatchStatus.FAILED) {
            throw new IllegalStateException(
                    "Solo se pueden reintentar batches en estado PROCESSING o FAILED, estado actual: "
                    + b.getStatus());
        }
        b.setStatus(BatchStatus.PENDING);
        b.setErrorMessage(null);
        b.setProcessedAt(null);
        log.info("Batch {} re-encolado a PENDING por acción admin", id);
        if (b.getZone() != null) b.getZone().getName();   // ← inicializa la zona antes de salir
        return batchRepository.save(b);
    }

    @Transactional
    public MeasurementBatch markFailed(UUID id, String reason) {
        MeasurementBatch b = batchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Batch", id));
        b.setStatus(BatchStatus.FAILED);
        b.setErrorMessage((reason != null && !reason.isBlank())
                ? reason : "Marcado como fallido manualmente");
        b.setProcessedAt(OffsetDateTime.now(DateRangeUtil.BOGOTA));
        log.info("Batch {} marcado como FAILED por acción admin: {}", id, b.getErrorMessage());
        return batchRepository.save(b);
    }

    /**
     * Elimina un batch junto con todos sus datos derivados en orden correcto:
     * alertas → resultados de cumplimiento → mediciones → batch.
     * Luego invalida la caché de KPIs del dashboard.
     */
    @Transactional
    public void delete(UUID id) {
        MeasurementBatch b = batchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Batch", id));

        List<UUID> zoneIds = measurementRepository.findZoneIdsByBatchId(id);
        log.info("Eliminando batch {} (zonas afectadas: {})", id, zoneIds);

        alertRepository.deleteByBatchId(id);
        complianceResultRepository.deleteByBatchId(id);
        measurementRepository.deleteByBatchId(id);
        batchRepository.delete(b);

        kpiCacheInvalidator.invalidateAll();
        log.info("Batch {} eliminado; caché de KPIs evictada", id);
    }
}
