package com.upc.acusticupc.ingestion.application.service;

import com.upc.acusticupc.auth.domain.model.User;
import com.upc.acusticupc.auth.domain.repository.UserRepository;
import com.upc.acusticupc.ingestion.application.dto.ParseResult;
import com.upc.acusticupc.ingestion.domain.model.ExcelFormat;
import com.upc.acusticupc.ingestion.domain.model.ParsedMeasurement;
import com.upc.acusticupc.ingestion.domain.port.SonometerParser;
import com.upc.acusticupc.shared.exception.DomainException;
import com.upc.acusticupc.shared.exception.ResourceNotFoundException;
import com.upc.acusticupc.sonometry.application.service.PeriodResolver;
import com.upc.acusticupc.sonometry.domain.model.BatchStatus;
import com.upc.acusticupc.sonometry.domain.model.Measurement;
import com.upc.acusticupc.sonometry.domain.model.MeasurementBatch;
import com.upc.acusticupc.sonometry.domain.repository.MeasurementBatchRepository;
import com.upc.acusticupc.sonometry.domain.repository.MeasurementRepository;
import com.upc.acusticupc.zones.domain.model.Zone;
import com.upc.acusticupc.zones.domain.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Orquesta el ciclo completo de ingesta de un archivo del sonometro:
 *  1. Crea el batch en estado PENDING.
 *  2. Dispara procesamiento @Async con virtual threads.
 *  3. Detecta formato → selecciona parser → parsea.
 *  4. Persiste mediciones en lotes.
 *  5. Actualiza el batch a COMPLETED o FAILED.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SonometerIngestionService {

    private final MeasurementBatchRepository batchRepository;
    private final MeasurementRepository measurementRepository;
    private final ZoneRepository zoneRepository;
    private final UserRepository userRepository;
    private final ExcelFormatDetector formatDetector;
    private final List<SonometerParser> parsers;
    private final PeriodResolver periodResolver;

    private static final int BATCH_INSERT_SIZE = 500;

    /**
     * Llamado sincrónicamente por el controller. Crea el batch y devuelve su UUID
     * antes de empezar el procesamiento.
     */
    @Transactional
    public UUID registerBatch(String fileName, UUID zoneId, String uploaderEmail) {
        Zone zone = zoneRepository.findById(zoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Zone", zoneId));

        User uploader = userRepository.findByEmailIgnoreCase(uploaderEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", uploaderEmail));

        MeasurementBatch batch = MeasurementBatch.builder()
                .fileName(fileName)
                .zone(zone)
                .uploadedBy(uploader)
                .status(BatchStatus.PENDING)
                .build();

        MeasurementBatch saved = batchRepository.save(batch);
        log.info("Batch {} registrado por {} para zona {}", saved.getId(), uploaderEmail, zone.getName());
        return saved.getId();
    }

    /**
     * Procesa el archivo de forma asíncrona usando virtual threads.
     * El archivo en `tempPath` se elimina al finalizar.
     */
    @Async("ingestionExecutor")
    public void processAsync(UUID batchId, Path tempPath) {
        log.info("Iniciando procesamiento asíncrono de batch {} en hilo {}", batchId, Thread.currentThread());
        try {
            markStatus(batchId, BatchStatus.PROCESSING, null);

            ParseResult result = parseFile(tempPath);
            int validInserted = persistMeasurements(batchId, result.measurements());

            completeBatch(batchId, result.totalRows(), validInserted, result.rejectedRows());
            log.info("Batch {} completado: {} válidas, {} rechazadas",
                    batchId, validInserted, result.rejectedRows());
        } catch (Exception e) {
            log.error("Batch {} falló: {}", batchId, e.getMessage(), e);
            markStatus(batchId, BatchStatus.FAILED, e.getMessage());
        } finally {
            try {
                Files.deleteIfExists(tempPath);
            } catch (IOException ignored) {}
        }
    }

    private ParseResult parseFile(Path tempPath) throws IOException {
        ExcelFormat format;
        try (InputStream raw = Files.newInputStream(tempPath);
             BufferedInputStream input = new BufferedInputStream(raw)) {
            format = formatDetector.detect(input);
        }

        // Si es XLSX, refinamos mirando A1
        if (format != ExcelFormat.TSV_STARTTIME) {
            format = refineXlsxFormat(tempPath);
        }

        SonometerParser parser = findParser(format);
        try (InputStream parseInput = Files.newInputStream(tempPath)) {
            return parser.parse(parseInput);
        }
    }

    private ExcelFormat refineXlsxFormat(Path tempPath) throws IOException {
        try (InputStream in = Files.newInputStream(tempPath);
             Workbook wb = new XSSFWorkbook(in)) {
            var firstSheet = wb.getSheetAt(0);
            var firstRow = firstSheet.getRow(0);
            if (firstRow == null) {
                throw new DomainException("Archivo XLSX sin filas en la primera hoja");
            }
            var firstCell = firstRow.getCell(0);
            String a1 = firstCell != null && firstCell.getCellType() == CellType.STRING
                    ? firstCell.getStringCellValue() : "";
            return formatDetector.refineXlsxFormat(a1);
        }
    }

    private SonometerParser findParser(ExcelFormat format) {
        return parsers.stream()
                .filter(p -> p.supportedFormat() == format)
                .findFirst()
                .orElseThrow(() -> new DomainException("No hay parser para el formato " + format));
    }

    @Transactional
    protected int persistMeasurements(UUID batchId, List<ParsedMeasurement> measurements) {
        MeasurementBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch", batchId));

        List<Measurement> buffer = new ArrayList<>(BATCH_INSERT_SIZE);
        int totalInserted = 0;

        for (ParsedMeasurement pm : measurements) {
            OffsetDateTime measuredAt = periodResolver.toColombiaOffset(pm.capturedAt());
            Measurement m = Measurement.builder()
                    .batch(batch)
                    .zone(batch.getZone())
                    .dbValue(pm.dbValue())
                    .unit(pm.unit())
                    .measuredAt(measuredAt)
                    .period(periodResolver.resolve(pm.capturedAt()))
                    .build();
            buffer.add(m);

            if (buffer.size() >= BATCH_INSERT_SIZE) {
                measurementRepository.saveAll(buffer);
                totalInserted += buffer.size();
                buffer.clear();
                log.debug("Batch {}: insertadas {} mediciones acumuladas", batchId, totalInserted);
            }
        }
        if (!buffer.isEmpty()) {
            measurementRepository.saveAll(buffer);
            totalInserted += buffer.size();
        }
        log.info("Batch {}: total persistido = {}", batchId, totalInserted);
        return totalInserted;
    }

    @Transactional
    protected void markStatus(UUID batchId, BatchStatus status, String errorMessage) {
        MeasurementBatch batch = batchRepository.findById(batchId).orElseThrow();
        batch.setStatus(status);
        if (errorMessage != null) {
            // truncar a 1000 chars para respetar el límite de la columna
            batch.setErrorMessage(errorMessage.length() > 1000 ? errorMessage.substring(0, 1000) : errorMessage);
        }
        batchRepository.save(batch);
    }

    @Transactional
    protected void completeBatch(UUID batchId, int totalRows, int validRows, int rejectedRows) {
        MeasurementBatch batch = batchRepository.findById(batchId).orElseThrow();
        batch.setStatus(BatchStatus.COMPLETED);
        batch.setTotalRows(totalRows);
        batch.setValidRows(validRows);
        batch.setRejectedRows(rejectedRows);
        batch.setProcessedAt(OffsetDateTime.now(PeriodResolver.COLOMBIA_OFFSET));
        batchRepository.save(batch);
    }
}
