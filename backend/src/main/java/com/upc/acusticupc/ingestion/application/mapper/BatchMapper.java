package com.upc.acusticupc.ingestion.application.mapper;

import com.upc.acusticupc.ingestion.application.dto.BatchSummaryDTO;
import com.upc.acusticupc.sonometry.domain.model.MeasurementBatch;
import org.springframework.stereotype.Component;

@Component
public class BatchMapper {

    public BatchSummaryDTO toSummary(MeasurementBatch batch) {
        return new BatchSummaryDTO(
                batch.getId(),
                batch.getFileName(),
                batch.getZone() != null ? batch.getZone().getName() : null,
                batch.getStatus(),
                batch.getTotalRows(),
                batch.getValidRows(),
                batch.getRejectedRows(),
                batch.getErrorMessage(),
                batch.getUploadedAt(),
                batch.getProcessedAt(),
                batch.getObservation()
        );
    }
}
