package com.upc.acusticupc.ingestion.application.service;

import com.upc.acusticupc.ingestion.domain.model.ParsedMeasurement;
import com.upc.acusticupc.shared.exception.ResourceNotFoundException;
import com.upc.acusticupc.sonometry.application.service.PeriodResolver;
import com.upc.acusticupc.sonometry.domain.model.Measurement;
import com.upc.acusticupc.sonometry.domain.model.MeasurementBatch;
import com.upc.acusticupc.sonometry.domain.repository.MeasurementBatchRepository;
import com.upc.acusticupc.zones.domain.model.Zone;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Persiste mediciones en una sola transacción usando JDBC batch de Hibernate.
 * Separado de SonometerIngestionService para evitar que la auto-invocación
 * desde @Async omita el proxy AOP y deje @Transactional sin efecto.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MeasurementBulkPersister {

    private static final int CHUNK_SIZE = 50;

    private final MeasurementBatchRepository batchRepository;
    private final PeriodResolver periodResolver;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public int persist(UUID batchId, List<ParsedMeasurement> measurements) {
        MeasurementBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch", batchId));
        UUID zoneId = batch.getZone().getId();

        int total = 0;
        for (ParsedMeasurement pm : measurements) {
            OffsetDateTime measuredAt = periodResolver.toColombiaOffset(pm.capturedAt());
            Measurement m = Measurement.builder()
                    .batch(entityManager.getReference(MeasurementBatch.class, batchId))
                    .zone(entityManager.getReference(Zone.class, zoneId))
                    .dbValue(pm.dbValue())
                    .unit(pm.unit())
                    .measuredAt(measuredAt)
                    .period(periodResolver.resolve(pm.capturedAt()))
                    .build();
            entityManager.persist(m);
            total++;

            if (total % CHUNK_SIZE == 0) {
                entityManager.flush();
                entityManager.clear();
                log.debug("Batch {}: flush/clear en {} filas", batchId, total);
            }
        }
        if (total % CHUNK_SIZE != 0) {
            entityManager.flush();
        }
        log.info("Batch {}: {} mediciones persistidas en chunks de {}", batchId, total, CHUNK_SIZE);
        return total;
    }
}
