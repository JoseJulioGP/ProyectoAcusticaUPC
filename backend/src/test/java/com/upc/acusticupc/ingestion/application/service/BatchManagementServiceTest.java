package com.upc.acusticupc.ingestion.application.service;

import com.upc.acusticupc.analytics.application.service.KpiCacheInvalidator;
import com.upc.acusticupc.compliance.domain.repository.AlertRepository;
import com.upc.acusticupc.compliance.domain.repository.ComplianceResultRepository;
import com.upc.acusticupc.ingestion.domain.model.IngestionFolder;
import com.upc.acusticupc.ingestion.domain.repository.IngestionFolderRepository;
import com.upc.acusticupc.shared.exception.ResourceNotFoundException;
import com.upc.acusticupc.sonometry.domain.model.MeasurementBatch;
import com.upc.acusticupc.sonometry.domain.repository.MeasurementBatchRepository;
import com.upc.acusticupc.sonometry.domain.repository.MeasurementRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BatchManagementServiceTest {

    @Mock MeasurementBatchRepository batchRepository;
    @Mock MeasurementRepository measurementRepository;
    @Mock AlertRepository alertRepository;
    @Mock ComplianceResultRepository complianceResultRepository;
    @Mock KpiCacheInvalidator kpiCacheInvalidator;
    @Mock IngestionFolderRepository folderRepository;
    @InjectMocks BatchManagementService service;

    private MeasurementBatch batch(UUID id) {
        return MeasurementBatch.builder().id(id).fileName("seed.xlsx").build();
    }

    // ---- updateObservation ----

    @Test
    void updateObservation_ok_seteaYGuarda() {
        UUID id = UUID.randomUUID();
        MeasurementBatch b = batch(id);
        when(batchRepository.findById(id)).thenReturn(Optional.of(b));

        service.updateObservation(id, "medición nocturna atípica");

        assertThat(b.getObservation()).isEqualTo("medición nocturna atípica");
        verify(batchRepository).save(b);
    }

    @Test
    void updateObservation_batchInexistente_lanzaResourceNotFound() {
        UUID id = UUID.randomUUID();
        when(batchRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateObservation(id, "x"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Batch");
    }

    // ---- moveToFolder ----

    @Test
    void moveToFolder_conFolder_asignaYGuarda() {
        UUID id = UUID.randomUUID();
        UUID folderId = UUID.randomUUID();
        MeasurementBatch b = batch(id);
        IngestionFolder folder = IngestionFolder.builder().id(folderId).name("Campaña").build();
        when(batchRepository.findById(id)).thenReturn(Optional.of(b));
        when(folderRepository.findById(folderId)).thenReturn(Optional.of(folder));

        service.moveToFolder(id, folderId);

        assertThat(b.getFolder()).isEqualTo(folder);
        verify(batchRepository).save(b);
    }

    @Test
    void moveToFolder_folderNull_desasigna() {
        UUID id = UUID.randomUUID();
        MeasurementBatch b = batch(id);
        when(batchRepository.findById(id)).thenReturn(Optional.of(b));

        service.moveToFolder(id, null);

        assertThat(b.getFolder()).isNull();
        verify(batchRepository).save(b);
    }

    @Test
    void moveToFolder_batchInexistente_lanzaResourceNotFound() {
        UUID id = UUID.randomUUID();
        when(batchRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.moveToFolder(id, UUID.randomUUID()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Batch");
    }

    @Test
    void moveToFolder_folderInexistente_lanzaResourceNotFound() {
        UUID id = UUID.randomUUID();
        UUID folderId = UUID.randomUUID();
        when(batchRepository.findById(id)).thenReturn(Optional.of(batch(id)));
        when(folderRepository.findById(folderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.moveToFolder(id, folderId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Folder");
    }
}
