package com.upc.acusticupc.ingestion.application.service;

import com.upc.acusticupc.analytics.application.service.KpiCacheInvalidator;
import com.upc.acusticupc.compliance.domain.repository.AlertRepository;
import com.upc.acusticupc.compliance.domain.repository.ComplianceResultRepository;
import com.upc.acusticupc.shared.exception.ResourceNotFoundException;
import com.upc.acusticupc.sonometry.domain.model.BatchStatus;
import com.upc.acusticupc.sonometry.domain.model.MeasurementBatch;
import com.upc.acusticupc.sonometry.domain.repository.MeasurementBatchRepository;
import com.upc.acusticupc.sonometry.domain.repository.MeasurementRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * Verifica el orden de la cascada en BatchManagementService.delete():
 * alertas → resultados cumplimiento → mediciones → batch → evict caché.
 */
@ExtendWith(MockitoExtension.class)
class BatchDeleteCascadeTest {

    @Mock MeasurementBatchRepository batchRepository;
    @Mock MeasurementRepository measurementRepository;
    @Mock AlertRepository alertRepository;
    @Mock ComplianceResultRepository complianceResultRepository;
    @Mock KpiCacheInvalidator kpiCacheInvalidator;

    @InjectMocks
    BatchManagementService service;

    @Test
    void delete_ejecutaCascadaEnOrdenCorrecto() {
        UUID batchId = UUID.randomUUID();
        UUID zoneId  = UUID.randomUUID();

        MeasurementBatch batch = new MeasurementBatch();
        batch.setId(batchId);
        batch.setStatus(BatchStatus.COMPLETED);

        when(batchRepository.findById(batchId)).thenReturn(Optional.of(batch));
        when(measurementRepository.findZoneIdsByBatchId(batchId)).thenReturn(List.of(zoneId));

        service.delete(batchId);

        InOrder orden = inOrder(alertRepository, complianceResultRepository,
                measurementRepository, batchRepository, kpiCacheInvalidator);

        orden.verify(alertRepository).deleteByBatchId(batchId);
        orden.verify(complianceResultRepository).deleteByBatchId(batchId);
        orden.verify(measurementRepository).deleteByBatchId(batchId);
        orden.verify(batchRepository).delete(batch);
        orden.verify(kpiCacheInvalidator).invalidateAll();
    }

    @Test
    void delete_lanzaExcepcionSiBatchNoExiste() {
        UUID batchId = UUID.randomUUID();
        when(batchRepository.findById(batchId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.delete(batchId));

        verifyNoInteractions(alertRepository);
        verifyNoInteractions(complianceResultRepository);
        verifyNoInteractions(measurementRepository);
        verifyNoInteractions(kpiCacheInvalidator);
    }

    @Test
    void retry_soloAceptaProcessingOFailed() {
        UUID batchId = UUID.randomUUID();
        MeasurementBatch batch = new MeasurementBatch();
        batch.setId(batchId);
        batch.setStatus(BatchStatus.COMPLETED);

        when(batchRepository.findById(batchId)).thenReturn(Optional.of(batch));

        assertThrows(IllegalStateException.class, () -> service.retry(batchId));
    }

    @Test
    void retry_conFailed_resetaAPending() {
        UUID batchId = UUID.randomUUID();
        MeasurementBatch batch = new MeasurementBatch();
        batch.setId(batchId);
        batch.setStatus(BatchStatus.FAILED);
        batch.setErrorMessage("error previo");

        when(batchRepository.findById(batchId)).thenReturn(Optional.of(batch));
        when(batchRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = service.retry(batchId);

        assert result.getStatus() == BatchStatus.PENDING;
        assert result.getErrorMessage() == null;
    }
}
