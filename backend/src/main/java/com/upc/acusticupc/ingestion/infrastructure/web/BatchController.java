package com.upc.acusticupc.ingestion.infrastructure.web;

import com.upc.acusticupc.ingestion.application.dto.BatchSummaryDTO;
import com.upc.acusticupc.ingestion.application.service.BatchQueryService;
import com.upc.acusticupc.ingestion.application.service.SonometerIngestionService;
import com.upc.acusticupc.ingestion.infrastructure.web.dto.BatchUploadResponse;
import com.upc.acusticupc.ingestion.infrastructure.web.dto.MeasurementResponseDTO;
import com.upc.acusticupc.ingestion.infrastructure.web.dto.PageResponse;
import com.upc.acusticupc.shared.exception.DomainException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ingest")
@RequiredArgsConstructor
@Slf4j
public class BatchController {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("xls", "xlsx");
    private static final Path UPLOAD_DIR = Path.of(System.getProperty("java.io.tmpdir"), "acusticupc-uploads");

    private final SonometerIngestionService ingestionService;
    private final BatchQueryService queryService;

    @PostMapping(value = "/batches", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BatchUploadResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("zoneId") UUID zoneId,
            @AuthenticationPrincipal UserDetails user
    ) throws IOException {

        validateFile(file);

        Files.createDirectories(UPLOAD_DIR);
        String ext = getExtension(file.getOriginalFilename());
        Path tempPath = UPLOAD_DIR.resolve(UUID.randomUUID() + "." + ext);
        file.transferTo(tempPath);
        log.info("Archivo '{}' guardado temporalmente en {}", file.getOriginalFilename(), tempPath);

        UUID batchId = ingestionService.registerBatch(
                file.getOriginalFilename(),
                zoneId,
                user.getUsername()  // username = email en CustomUserDetailsService
        );

        ingestionService.processAsync(batchId, tempPath);

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(BatchUploadResponse.pending(batchId));
    }

    @GetMapping("/batches")
    @PreAuthorize("isAuthenticated()")
    public PageResponse<BatchSummaryDTO> list(
            @PageableDefault(size = 20, sort = "uploadedAt") Pageable pageable
    ) {
        return PageResponse.from(queryService.listBatches(pageable), b -> b);
    }

    @GetMapping("/batches/{id}")
    @PreAuthorize("isAuthenticated()")
    public BatchSummaryDTO get(@PathVariable UUID id) {
        return queryService.getBatch(id);
    }

    @GetMapping("/batches/{id}/measurements")
    @PreAuthorize("isAuthenticated()")
    public PageResponse<MeasurementResponseDTO> getMeasurements(
            @PathVariable UUID id,
            @PageableDefault(size = 50, sort = "measuredAt") Pageable pageable
    ) {
        return PageResponse.from(queryService.getMeasurements(id, pageable), MeasurementResponseDTO::from);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new DomainException("Archivo vacío");
        }
        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isBlank()) {
            throw new DomainException("Nombre de archivo inválido");
        }
        String ext = getExtension(fileName);
        if (!ALLOWED_EXTENSIONS.contains(ext.toLowerCase())) {
            throw new DomainException("Solo se aceptan archivos .xls o .xlsx (recibido '." + ext + "')");
        }
    }

    private String getExtension(String fileName) {
        int dotIdx = fileName.lastIndexOf('.');
        return dotIdx < 0 ? "" : fileName.substring(dotIdx + 1);
    }
}