package com.upc.acusticupc.analytics.application.service;

import com.upc.acusticupc.analytics.domain.dto.DashboardKpisDTO;
import com.upc.acusticupc.analytics.domain.repository.AlertStatsRepository;
import com.upc.acusticupc.analytics.domain.repository.MeasurementStatsRepository;
import com.upc.acusticupc.compliance.domain.model.AlertSeverity;
import com.upc.acusticupc.compliance.domain.model.ComplianceStatus;
import com.upc.acusticupc.compliance.domain.repository.ComplianceResultRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KpiServiceTest {

    @Mock
    private MeasurementStatsRepository measurementStatsRepository;

    @Mock
    private AlertStatsRepository alertStatsRepository;

    @Mock
    private ComplianceResultRepository complianceResultRepository;

    @InjectMocks
    private KpiService kpiService;

    private OffsetDateTime from;
    private OffsetDateTime to;

    @BeforeEach
    void setUp() {
        from = OffsetDateTime.parse("2026-04-01T00:00:00-05:00");
        to = OffsetDateTime.parse("2026-04-30T23:59:59-05:00");
    }

    @Test
    void computeKpis_sumaCorrectamenteAlertasPorSeveridad() {
        when(measurementStatsRepository.countInRange(any(), any(), any(), any())).thenReturn(1000L);
        when(measurementStatsRepository.countActiveZones(any(), any())).thenReturn(3);

        // 2 CRITICAS, 5 MODERADAS, 1 LEVE = 8 total
        when(alertStatsRepository.countBySeverityInRange(any(), any())).thenReturn(List.of(
                new Object[]{AlertSeverity.CRITICA, 2L},
                new Object[]{AlertSeverity.MODERADA, 5L},
                new Object[]{AlertSeverity.LEVE, 1L}
        ));

        when(complianceResultRepository.countByEvaluatedAtBetween(any(), any())).thenReturn(10L);
        when(complianceResultRepository.countByEvaluatedAtBetweenAndStatus(any(), any(), any()))
                .thenReturn(8L);

        DashboardKpisDTO result = kpiService.computeKpis(from, to, null, null);

        assertThat(result.totalMeasurements()).isEqualTo(1000L);
        assertThat(result.zonesActive()).isEqualTo(3);
        assertThat(result.alertsActive()).isEqualTo(8L);
        assertThat(result.alertsBySeverity().get(AlertSeverity.CRITICA)).isEqualTo(2L);
        assertThat(result.alertsBySeverity().get(AlertSeverity.MODERADA)).isEqualTo(5L);
        assertThat(result.alertsBySeverity().get(AlertSeverity.LEVE)).isEqualTo(1L);
    }

    @Test
    void computeKpis_complianceRate0CuandoNoHayResults() {
        when(measurementStatsRepository.countInRange(any(), any(), any(), any())).thenReturn(0L);
        when(measurementStatsRepository.countActiveZones(any(), any())).thenReturn(0);
        when(alertStatsRepository.countBySeverityInRange(any(), any())).thenReturn(List.of());
        when(complianceResultRepository.countByEvaluatedAtBetween(any(), any())).thenReturn(0L);
        when(complianceResultRepository.countByEvaluatedAtBetweenAndStatus(any(), any(), any()))
                .thenReturn(0L);

        DashboardKpisDTO result = kpiService.computeKpis(from, to, null, null);

        assertThat(result.complianceRatePercent()).isEqualTo(0.0);
    }

    @Test
    void computeKpis_complianceRate100CuandoTodosCumplen() {
        when(measurementStatsRepository.countInRange(any(), any(), any(), any())).thenReturn(500L);
        when(measurementStatsRepository.countActiveZones(any(), any())).thenReturn(2);
        when(alertStatsRepository.countBySeverityInRange(any(), any())).thenReturn(List.of());
        when(complianceResultRepository.countByEvaluatedAtBetween(any(), any())).thenReturn(5L);
        when(complianceResultRepository.countByEvaluatedAtBetweenAndStatus(
                any(), any(), eq(ComplianceStatus.CUMPLE))).thenReturn(5L);

        DashboardKpisDTO result = kpiService.computeKpis(from, to, null, null);

        assertThat(result.complianceRatePercent()).isEqualTo(100.0);
    }

    private static <T> T eq(T value) {
        return org.mockito.ArgumentMatchers.eq(value);
    }
}