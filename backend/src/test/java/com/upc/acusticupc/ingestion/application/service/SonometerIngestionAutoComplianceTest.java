package com.upc.acusticupc.ingestion.application.service;

import com.upc.acusticupc.auth.domain.repository.UserRepository;
import com.upc.acusticupc.compliance.application.service.ComplianceEngine;
import com.upc.acusticupc.sonometry.application.service.PeriodResolver;
import com.upc.acusticupc.sonometry.domain.repository.MeasurementBatchRepository;
import com.upc.acusticupc.zones.domain.repository.ZoneRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Sprint 8 · {@link SonometerIngestionService#triggerAutoCompliance(UUID, int)}
 * dispara el cálculo de cumplimiento cuando el batch tiene mediciones válidas,
 * y no lo dispara cuando quedó vacío.
 */
@ExtendWith(MockitoExtension.class)
class SonometerIngestionAutoComplianceTest {

    @Mock MeasurementBatchRepository batchRepository;
    @Mock ZoneRepository zoneRepository;
    @Mock UserRepository userRepository;
    @Mock ExcelFormatDetector formatDetector;
    @Mock PeriodResolver periodResolver;
    @Mock MeasurementBulkPersister bulkPersister;
    @Mock YearRangeValidator yearValidator;
    @Mock ComplianceEngine complianceEngine;

    private SonometerIngestionService newService() {
        return new SonometerIngestionService(
                batchRepository, zoneRepository, userRepository,
                formatDetector, List.of(),
                periodResolver, bulkPersister, yearValidator, complianceEngine);
    }

    @Test
    void triggerAutoCompliance_conMedicionesValidas_invocaEngine() {
        UUID batchId = UUID.randomUUID();

        newService().triggerAutoCompliance(batchId, 100);

        verify(complianceEngine).evaluateBatch(batchId);
    }

    @Test
    void triggerAutoCompliance_sinMedicionesValidas_noInvocaEngine() {
        UUID batchId = UUID.randomUUID();

        newService().triggerAutoCompliance(batchId, 0);

        verify(complianceEngine, never()).evaluateBatch(any());
    }
}
