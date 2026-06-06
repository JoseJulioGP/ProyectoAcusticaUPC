package com.upc.acusticupc.compliance.application.service;

import com.upc.acusticupc.analytics.application.service.KpiCacheInvalidator;
import com.upc.acusticupc.compliance.application.calculator.L90Calculator;
import com.upc.acusticupc.compliance.application.calculator.LAeqCalculator;
import com.upc.acusticupc.compliance.domain.repository.AlertRepository;
import com.upc.acusticupc.compliance.domain.repository.ComplianceResultRepository;
import com.upc.acusticupc.compliance.domain.repository.NoiseStandardRepository;
import com.upc.acusticupc.shared.exception.DomainException;
import com.upc.acusticupc.sonometry.domain.model.MeasurementBatch;
import com.upc.acusticupc.sonometry.domain.repository.MeasurementBatchRepository;
import com.upc.acusticupc.sonometry.domain.repository.MeasurementRepository;
import com.upc.acusticupc.zones.domain.model.Zone;
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
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Sprint 8 · Idempotencia de {@link ComplianceEngine#evaluateBatch(UUID)}.
 * Antes de recalcular, se borran las alertas y resultados anteriores de ese
 * batch para no duplicar al re-evaluar.
 */
@ExtendWith(MockitoExtension.class)
class ComplianceEngineIdempotencyTest {

    @Mock MeasurementBatchRepository batchRepository;
    @Mock MeasurementRepository measurementRepository;
    @Mock NoiseStandardRepository noiseStandardRepository;
    @Mock ComplianceResultRepository resultRepository;
    @Mock AlertRepository alertRepository;
    @Mock LAeqCalculator laeqCalculator;
    @Mock L90Calculator l90Calculator;
    @Mock KpiCacheInvalidator kpiCacheInvalidator;

    @InjectMocks ComplianceEngine engine;

    @Test
    void evaluateBatch_borraAlertasYResultadosPreviosAntesDeRecalcular() {
        UUID batchId = UUID.randomUUID();
        MeasurementBatch batch = MeasurementBatch.builder()
                .id(batchId)
                .zone(new Zone())
                .build();
        when(batchRepository.findById(batchId)).thenReturn(Optional.of(batch));
        when(measurementRepository.findByBatchId(batchId)).thenReturn(List.of());

        // Batch sin mediciones: lanza DomainException tras borrar previos.
        assertThrows(DomainException.class, () -> engine.evaluateBatch(batchId));

        // Orden requerido para FK safety: alertas antes que resultados.
        InOrder ord = inOrder(alertRepository, resultRepository);
        ord.verify(alertRepository).deleteByBatchId(batchId);
        ord.verify(resultRepository).deleteByBatchId(batchId);

        // No se invalida la caché si la evaluación falla.
        verify(kpiCacheInvalidator, never()).invalidateAll();
    }
}
