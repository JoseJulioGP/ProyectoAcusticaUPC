package com.upc.acusticupc.analytics.application.service;

import com.upc.acusticupc.analytics.domain.dto.Granularity;
import com.upc.acusticupc.analytics.domain.dto.TimeSeriesPointDTO;
import com.upc.acusticupc.analytics.domain.repository.MeasurementStatsRepository;
import com.upc.acusticupc.sonometry.domain.model.Period;
import com.upc.acusticupc.zones.domain.model.Zone;
import com.upc.acusticupc.zones.domain.repository.ZoneRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TimeSeriesServiceTest {

    @Mock MeasurementStatsRepository measurementStatsRepository;
    @Mock ZoneRepository zoneRepository;

    @InjectMocks TimeSeriesService service;

    private static final OffsetDateTime FROM = OffsetDateTime.parse("2026-04-01T00:00:00-05:00");
    private static final OffsetDateTime TO = OffsetDateTime.parse("2026-04-30T23:59:59-05:00");

    @Test
    void series_conGranularidadHOUR_devuelvePuntosBienOrdenadosPorBucket() {
        UUID zoneId = UUID.randomUUID();
        Zone zone = org.mockito.Mockito.mock(Zone.class);
        when(zone.getId()).thenReturn(zoneId);
        when(zone.getName()).thenReturn("Bloque A");
        when(zoneRepository.findAll()).thenReturn(List.of(zone));

        // El driver pg moderno entrega el bucket de date_trunc como Instant
        Instant t1 = Instant.parse("2026-04-01T10:00:00Z");
        Instant t2 = Instant.parse("2026-04-01T11:00:00Z");
        List<Object[]> rows = List.of(
                new Object[]{t1, zoneId, 63.456, 100L, "DIURNO"},
                new Object[]{t2, zoneId, 70.0, 50L, "DIURNO"}
        );
        when(measurementStatsRepository.laeqTimeSeries(anyString(), any(), any(), any(), any()))
                .thenReturn(rows);

        List<TimeSeriesPointDTO> result =
                service.series(FROM, TO, null, null, Granularity.HOUR);

        assertThat(result).hasSize(2);
        TimeSeriesPointDTO first = result.get(0);
        assertThat(first.zoneName()).isEqualTo("Bloque A");
        assertThat(first.zoneId()).isEqualTo(zoneId);
        assertThat(first.laeqDb()).isEqualTo(63.5); // redondeado a 1 decimal
        assertThat(first.sampleCount()).isEqualTo(100L);
        assertThat(first.period()).isEqualTo(Period.DIURNO);
        assertThat(first.bucket()).isEqualTo(t1.atOffset(ZoneOffset.of("-05:00")));
        // Orden preservado: primer bucket es anterior al segundo
        assertThat(first.bucket()).isBefore(result.get(1).bucket());
    }

    @Test
    void series_zonaInexistenteEnCache_devuelveNombreFallback() {
        when(zoneRepository.findAll()).thenReturn(List.of()); // cache vacío

        UUID huerfana = UUID.randomUUID();
        List<Object[]> rows = List.<Object[]>of(
                new Object[]{Timestamp.from(Instant.parse("2026-04-01T10:00:00Z")),
                        huerfana, 55.0, 10L, "NOCTURNO"}
        );
        when(measurementStatsRepository.laeqTimeSeries(anyString(), any(), any(), any(), any()))
                .thenReturn(rows);

        List<TimeSeriesPointDTO> result =
                service.series(FROM, TO, null, null, Granularity.HOUR);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).zoneName()).isEqualTo("Zona desconocida");
    }

    @Test
    void series_sinDatos_devuelveListaVacia() {
        when(zoneRepository.findAll()).thenReturn(List.of());
        when(measurementStatsRepository.laeqTimeSeries(anyString(), any(), any(), any(), any()))
                .thenReturn(List.of());

        List<TimeSeriesPointDTO> result =
                service.series(FROM, TO, null, null, Granularity.DAY);

        assertThat(result).isEmpty();
    }
}
