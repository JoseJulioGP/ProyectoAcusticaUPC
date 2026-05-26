package com.upc.acusticupc.compliance.application.service;

import com.upc.acusticupc.compliance.application.calculator.L90Calculator;
import com.upc.acusticupc.compliance.application.calculator.LAeqCalculator;
import com.upc.acusticupc.compliance.domain.model.*;
import com.upc.acusticupc.compliance.domain.repository.AlertRepository;
import com.upc.acusticupc.compliance.domain.repository.ComplianceResultRepository;
import com.upc.acusticupc.compliance.domain.repository.NoiseStandardRepository;
import com.upc.acusticupc.shared.exception.DomainException;
import com.upc.acusticupc.shared.exception.ResourceNotFoundException;
import com.upc.acusticupc.sonometry.domain.model.Measurement;
import com.upc.acusticupc.sonometry.domain.model.MeasurementBatch;
import com.upc.acusticupc.sonometry.domain.model.Period;
import com.upc.acusticupc.sonometry.domain.repository.MeasurementBatchRepository;
import com.upc.acusticupc.sonometry.domain.repository.MeasurementRepository;
import com.upc.acusticupc.zones.domain.model.Zone;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ComplianceEngine {

    private final MeasurementBatchRepository batchRepository;
    private final MeasurementRepository measurementRepository;
    private final NoiseStandardRepository noiseStandardRepository;
    private final ComplianceResultRepository resultRepository;
    private final AlertRepository alertRepository;
    private final LAeqCalculator laeqCalculator;
    private final L90Calculator l90Calculator;

    /**
     * Evalua un batch ya COMPLETED. Por cada period (DIURNO/NOCTURNO) que tenga
     * mediciones, calcula LAeq + L90, compara con el estandar aplicable, y persiste
     * un ComplianceResult + opcionalmente un Alert.
     *
     * Fire-and-forget: @Async exige retorno void o Future, por eso no devuelve
     * la lista. Los resultados se consultan via GET /compliance/results/batch/{id}.
     */
    @Async
    @Transactional
    public void evaluateBatch(UUID batchId) {
        MeasurementBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch", batchId));

        Zone zone = batch.getZone();
        log.info("Evaluando cumplimiento batch={} zona={}", batchId, zone.getName());

        List<Measurement> all = measurementRepository.findByBatchId(batchId);
        if (all.isEmpty()) {
            throw new DomainException("Batch sin mediciones: nada que evaluar");
        }

        // Agrupar por period
        Map<Period, List<Measurement>> byPeriod = new EnumMap<>(Period.class);
        for (Measurement m : all) {
            byPeriod.computeIfAbsent(m.getPeriod(), p -> new ArrayList<>()).add(m);
        }

        int evaluated = 0;
        for (Map.Entry<Period, List<Measurement>> e : byPeriod.entrySet()) {
            evaluatePeriod(batch, zone, e.getKey(), e.getValue());
            evaluated++;
        }
        log.info("Cumplimiento batch={} evaluado: {} period(s)", batchId, evaluated);
    }

    private ComplianceResult evaluatePeriod(MeasurementBatch batch, Zone zone,
                                           Period period, List<Measurement> measurements) {
        List<Double> dbValues = measurements.stream()
                .map(Measurement::getDbValue)
                .toList();

        BigDecimal laeq = laeqCalculator.calculate(dbValues);
        BigDecimal l90  = l90Calculator.calculate(dbValues);
        double min = dbValues.stream().mapToDouble(Double::doubleValue).min().orElseThrow();
        double max = dbValues.stream().mapToDouble(Double::doubleValue).max().orElseThrow();

        NoiseStandard standard = lookupStandard(zone, period, "AMBIENT");
        BigDecimal limit = BigDecimal.valueOf(standard.getMaxDb());

        ComplianceStatus status = (laeq.compareTo(limit) > 0)
                ? ComplianceStatus.NO_CUMPLE
                : ComplianceStatus.CUMPLE;

        OffsetDateTime from = measurements.stream()
                .map(Measurement::getMeasuredAt).min(OffsetDateTime::compareTo).orElseThrow();
        OffsetDateTime to = measurements.stream()
                .map(Measurement::getMeasuredAt).max(OffsetDateTime::compareTo).orElseThrow();

        ComplianceResult result = ComplianceResult.builder()
                .zone(zone)
                .batch(batch)
                .period(period)
                .measurementCount(measurements.size())
                .laeqDb(laeq)
                .l90Db(l90)
                .minDb(BigDecimal.valueOf(min).setScale(2, java.math.RoundingMode.HALF_UP))
                .maxDb(BigDecimal.valueOf(max).setScale(2, java.math.RoundingMode.HALF_UP))
                .standardDb(limit)
                .status(status)
                .evaluatedFrom(from)
                .evaluatedTo(to)
                .evaluatedAt(OffsetDateTime.now())
                .standardType("AMBIENT")
                .build();

        result = resultRepository.save(result);

        if (status == ComplianceStatus.NO_CUMPLE) {
            double excess = laeq.doubleValue() - limit.doubleValue();
            Alert alert = Alert.builder()
                    .zone(zone)
                    .batch(batch)
                    .complianceResult(result)
                    .period(period)
                    .measuredDb(laeq)
                    .standardDb(limit)
                    .severity(AlertSeverity.fromExcess(excess))
                    .triggeredAt(OffsetDateTime.now())
                    .notes(String.format("LAeq=%.2f dB excede estandar de %.2f dB en %.2f dB",
                            laeq.doubleValue(), limit.doubleValue(), excess))
                    .build();
            alertRepository.save(alert);
            log.warn("ALERTA generada: zona={} period={} excess={}dB severidad={}",
                    zone.getName(), period, excess, alert.getSeverity());
        }
        return result;
    }

    /**
     * Busca el estandar aplicable al sector/subsector/period de la zona y al tipo dado.
     * Asume que zone.getSector() y zone.getSubsector() coinciden con los valores
     * sembrados en noise_standards. Si una zona apunta a un sector/subsector no
     * sembrado, lanza excepcion clara.
     */
    private NoiseStandard lookupStandard(Zone zone, Period period, String standardType) {
        return noiseStandardRepository
                .findBySectorAndSubsectorAndPeriodAndStandardType(
                        zone.getSector(),
                        zone.getSubsector(),
                        period,
                        StandardType.valueOf(standardType))
                .orElseThrow(() -> new DomainException(String.format(
                        "No hay noise_standard para sector=%s subsector=%s period=%s tipo=%s. " +
                        "Revisa que la zona este bien clasificada y que la migracion V4 esta aplicada.",
                        zone.getSector(), zone.getSubsector(), period, standardType)));
    }
}