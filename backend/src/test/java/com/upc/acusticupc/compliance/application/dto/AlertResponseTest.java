package com.upc.acusticupc.compliance.application.dto;

import com.upc.acusticupc.compliance.domain.model.Alert;
import com.upc.acusticupc.compliance.domain.model.AlertSeverity;
import com.upc.acusticupc.sonometry.domain.model.MeasurementBatch;
import com.upc.acusticupc.sonometry.domain.model.Period;
import com.upc.acusticupc.zones.domain.model.Zone;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Sprint 8 · Verifica que {@link AlertResponse#from(Alert)} extrae correctamente
 * la observación del batch ligado a la alerta (campo nuevo {@code observation}).
 */
class AlertResponseTest {

    private Alert buildAlert(MeasurementBatch batch) {
        Zone zone = new Zone();
        zone.setId(UUID.randomUUID());
        zone.setName("Bloque A");
        return Alert.builder()
                .id(UUID.randomUUID())
                .zone(zone)
                .batch(batch)
                .period(Period.NOCTURNO)
                .measuredDb(BigDecimal.valueOf(72.5))
                .standardDb(BigDecimal.valueOf(50.0))
                .severity(AlertSeverity.CRITICA)
                .triggeredAt(OffsetDateTime.parse("2026-04-01T22:00:00-05:00"))
                .build();
    }

    @Test
    void from_conBatchConObservacion_extraeObservacion() {
        MeasurementBatch batch = MeasurementBatch.builder()
                .observation("Obras de construcción en frente")
                .build();

        AlertResponse response = AlertResponse.from(buildAlert(batch));

        assertEquals("Obras de construcción en frente", response.observation());
    }

    @Test
    void from_conBatchSinObservacion_devuelveNull() {
        MeasurementBatch batch = MeasurementBatch.builder().observation(null).build();

        AlertResponse response = AlertResponse.from(buildAlert(batch));

        assertNull(response.observation());
    }

    @Test
    void from_sinBatch_devuelveNull() {
        AlertResponse response = AlertResponse.from(buildAlert(null));

        assertNull(response.observation());
    }
}
