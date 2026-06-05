package com.upc.acusticupc.analytics;

import com.upc.acusticupc.support.AbstractPostgresIT;
import com.upc.acusticupc.sonometry.domain.model.BatchStatus;
import com.upc.acusticupc.sonometry.domain.model.Measurement;
import com.upc.acusticupc.sonometry.domain.model.MeasurementBatch;
import com.upc.acusticupc.sonometry.domain.model.Period;
import com.upc.acusticupc.sonometry.domain.repository.MeasurementBatchRepository;
import com.upc.acusticupc.sonometry.domain.repository.MeasurementRepository;
import com.upc.acusticupc.zones.domain.model.Sector;
import com.upc.acusticupc.zones.domain.model.Subsector;
import com.upc.acusticupc.zones.domain.model.Zone;
import com.upc.acusticupc.zones.domain.repository.ZoneRepository;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.OffsetDateTime;

import static org.hamcrest.Matchers.closeTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * IT con datos seed reales para /weekday y /before-after. Ejecuta SQL nativo
 * Postgres (EXTRACT(DOW), date_trunc) → en C8.2 extiende AbstractPostgresIT
 * (Testcontainers) porque H2 no soporta EXTRACT(DOW).
 */
@Disabled("requires Docker default socket for Testcontainers-Postgres; H2 no soporta EXTRACT(DOW). Ver DEUDA_TECNICA.md")
class AnalyticsTrendControllerIntegrationTest extends AbstractPostgresIT {

    @Autowired private WebApplicationContext context;
    @Autowired private ZoneRepository zoneRepository;
    @Autowired private MeasurementBatchRepository batchRepository;
    @Autowired private MeasurementRepository measurementRepository;

    private MockMvc mockMvc;

    @PostConstruct
    void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @BeforeEach
    void seed() {
        measurementRepository.deleteAll();
        batchRepository.deleteAll();
        zoneRepository.deleteAll();

        Zone zone = zoneRepository.save(Zone.builder()
                .name("Bloque A").sector(Sector.B_TRANQUILIDAD_RUIDO_MODERADO)
                .subsector(Subsector.UNIVERSIDADES_COLEGIOS).active(true).build());

        MeasurementBatch batch = batchRepository.save(MeasurementBatch.builder()
                .fileName("seed.xlsx").zone(zone).status(BatchStatus.COMPLETED)
                .uploadedAt(OffsetDateTime.now()).build());

        // Lunes 2026-03-02: 60 y 70 dB (avg 65). Martes 2026-03-03: 50 dB.
        save(zone, batch, "2026-03-02T10:00:00-05:00", 60.0);
        save(zone, batch, "2026-03-02T11:00:00-05:00", 70.0);
        save(zone, batch, "2026-03-03T10:00:00-05:00", 50.0);
    }

    private void save(Zone zone, MeasurementBatch batch, String ts, double db) {
        measurementRepository.save(Measurement.builder()
                .zone(zone).batch(batch).dbValue(db)
                .measuredAt(OffsetDateTime.parse(ts)).period(Period.DIURNO).build());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void weekday_agregaPromedioPorDiaDeSemana() throws Exception {
        // DOW: 1=lunes, 2=martes (Postgres). Orden ASC por dow.
        mockMvc.perform(get("/api/v1/analytics/weekday")
                        .param("year", "2026")
                        .param("isoWeekFrom", "1")
                        .param("isoWeekTo", "53"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].weekday").value(1))
                .andExpect(jsonPath("$[0].avgDb", closeTo(65.0, 0.001)))
                .andExpect(jsonPath("$[1].weekday").value(2))
                .andExpect(jsonPath("$[1].avgDb", closeTo(50.0, 0.001)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void beforeAfter_parteSerieDiariaPorPivote() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/before-after")
                        .param("pivot", "2026-03-03T00:00:00-05:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.before[0].avgDb", closeTo(65.0, 0.001)))
                .andExpect(jsonPath("$.after[0].avgDb", closeTo(50.0, 0.001)));
    }
}
