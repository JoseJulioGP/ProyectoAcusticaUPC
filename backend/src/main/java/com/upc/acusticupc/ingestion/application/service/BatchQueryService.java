package com.upc.acusticupc.ingestion.application.service;

import com.upc.acusticupc.ingestion.application.dto.BatchSummaryDTO;
import com.upc.acusticupc.ingestion.application.mapper.BatchMapper;
import com.upc.acusticupc.shared.exception.ResourceNotFoundException;
import com.upc.acusticupc.sonometry.domain.model.Measurement;
import com.upc.acusticupc.sonometry.domain.repository.MeasurementBatchRepository;
import com.upc.acusticupc.sonometry.domain.repository.MeasurementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BatchQueryService {

    private final MeasurementBatchRepository batchRepository;
    private final MeasurementRepository measurementRepository;
    private final BatchMapper batchMapper;

    public Page<BatchSummaryDTO> listBatches(Pageable pageable) {
        return batchRepository.findAll(pageable).map(batchMapper::toSummary);
    }

    public BatchSummaryDTO getBatch(UUID id) {
        return batchRepository.findById(id)
                .map(batchMapper::toSummary)
                .orElseThrow(() -> new ResourceNotFoundException("Batch", id));
    }

    public Page<Measurement> getMeasurements(UUID batchId, Pageable pageable) {
        if (!batchRepository.existsById(batchId)) {
            throw new ResourceNotFoundException("Batch", batchId);
        }
        return measurementRepository.findByBatchId(batchId, pageable);
    }
}
