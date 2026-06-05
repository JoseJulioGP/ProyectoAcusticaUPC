package com.upc.acusticupc.ingestion.application.service;

import com.upc.acusticupc.ingestion.domain.model.IngestionFolder;
import com.upc.acusticupc.ingestion.domain.repository.IngestionFolderRepository;
import com.upc.acusticupc.ingestion.infrastructure.web.dto.FolderRequest;
import com.upc.acusticupc.ingestion.infrastructure.web.dto.FolderResponse;
import com.upc.acusticupc.shared.exception.ResourceNotFoundException;
import com.upc.acusticupc.sonometry.domain.repository.MeasurementBatchRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FolderServiceTest {

    @Mock IngestionFolderRepository folderRepository;
    @Mock MeasurementBatchRepository batchRepository;
    @Mock com.upc.acusticupc.auth.domain.repository.UserRepository userRepository;
    @InjectMocks FolderService service;

    private IngestionFolder folder(UUID id) {
        return IngestionFolder.builder().id(id).name("Campaña 2026").build();
    }

    @Test
    void delete_conSubcarpetas_lanzaFolderHasChildren() {
        UUID id = UUID.randomUUID();
        when(folderRepository.findById(id)).thenReturn(Optional.of(folder(id)));
        when(folderRepository.existsByParentId(id)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(id))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("FOLDER_HAS_CHILDREN");

        verify(folderRepository, never()).delete(any());
    }

    @Test
    void delete_conBatches_lanzaFolderInUse() {
        UUID id = UUID.randomUUID();
        when(folderRepository.findById(id)).thenReturn(Optional.of(folder(id)));
        when(folderRepository.existsByParentId(id)).thenReturn(false);
        when(batchRepository.existsByFolderId(id)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(id))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("FOLDER_IN_USE");

        verify(folderRepository, never()).delete(any());
    }

    @Test
    void delete_sinDependencias_borra() {
        UUID id = UUID.randomUUID();
        IngestionFolder f = folder(id);
        when(folderRepository.findById(id)).thenReturn(Optional.of(f));
        when(folderRepository.existsByParentId(id)).thenReturn(false);
        when(batchRepository.existsByFolderId(id)).thenReturn(false);

        service.delete(id);

        verify(folderRepository).delete(f);
    }

    @Test
    void delete_inexistente_lanzaResourceNotFound() {
        UUID id = UUID.randomUUID();
        when(folderRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Folder");
    }

    @Test
    void create_carpetaRaiz_guardaYDevuelveResponse() {
        UUID id = UUID.randomUUID();
        when(userRepository.findByEmailIgnoreCase("autor@upc.edu.co")).thenReturn(Optional.empty());
        when(folderRepository.save(any())).thenReturn(folder(id));

        FolderResponse res = service.create(new FolderRequest("Campaña 2026", null), "autor@upc.edu.co");

        assertThat(res.name()).isEqualTo("Campaña 2026");
        assertThat(res.parentId()).isNull();
        verify(folderRepository).save(any());
    }
}
