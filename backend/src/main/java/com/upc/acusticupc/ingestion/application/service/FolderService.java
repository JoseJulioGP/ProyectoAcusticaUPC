package com.upc.acusticupc.ingestion.application.service;

import com.upc.acusticupc.auth.domain.model.User;
import com.upc.acusticupc.auth.domain.repository.UserRepository;
import com.upc.acusticupc.ingestion.domain.model.IngestionFolder;
import com.upc.acusticupc.ingestion.domain.repository.IngestionFolderRepository;
import com.upc.acusticupc.ingestion.infrastructure.web.dto.FolderRequest;
import com.upc.acusticupc.ingestion.infrastructure.web.dto.FolderResponse;
import com.upc.acusticupc.shared.exception.ResourceNotFoundException;
import com.upc.acusticupc.sonometry.domain.repository.MeasurementBatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FolderService {

    private final IngestionFolderRepository folderRepository;
    private final MeasurementBatchRepository batchRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<FolderResponse> list() {
        return folderRepository.findAll().stream().map(FolderResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public FolderResponse get(UUID id) {
        return FolderResponse.from(findOrThrow(id));
    }

    @Transactional
    public FolderResponse create(FolderRequest req, String creatorEmail) {
        User creator = (creatorEmail == null) ? null
                : userRepository.findByEmailIgnoreCase(creatorEmail).orElse(null);
        IngestionFolder folder = IngestionFolder.builder()
                .name(req.name())
                .parent(resolveParent(req.parentId()))
                .createdBy(creator)
                .build();
        folder = folderRepository.save(folder);
        log.info("Carpeta creada: {} (id={}) por {}", folder.getName(), folder.getId(), creatorEmail);
        return FolderResponse.from(folder);
    }

    @Transactional
    public FolderResponse update(UUID id, FolderRequest req) {
        IngestionFolder folder = findOrThrow(id);
        folder.setName(req.name());
        folder.setParent(resolveParent(req.parentId()));
        return FolderResponse.from(folderRepository.save(folder));
    }

    @Transactional
    public void delete(UUID id) {
        IngestionFolder folder = findOrThrow(id);
        if (folderRepository.existsByParentId(id)) {
            throw new IllegalStateException(
                    "FOLDER_HAS_CHILDREN: la carpeta tiene subcarpetas; elimínalas o muévelas primero");
        }
        if (batchRepository.existsByFolderId(id)) {
            throw new IllegalStateException(
                    "FOLDER_IN_USE: la carpeta tiene batches asociados; reasígnalos a otra carpeta primero");
        }
        folderRepository.delete(folder);
        log.info("Carpeta eliminada: {} (id={})", folder.getName(), id);
    }

    private IngestionFolder findOrThrow(UUID id) {
        return folderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Folder", id));
    }

    private IngestionFolder resolveParent(UUID parentId) {
        if (parentId == null) return null;
        return folderRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("Folder", parentId));
    }
}
