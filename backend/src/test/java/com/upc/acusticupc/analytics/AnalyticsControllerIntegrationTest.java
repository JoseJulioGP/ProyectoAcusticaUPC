package com.upc.acusticupc.analytics;

import com.upc.acusticupc.analytics.application.service.AlertStatsService;
import com.upc.acusticupc.analytics.application.service.KpiService;
import com.upc.acusticupc.analytics.application.service.ZoneDetailService;
import com.upc.acusticupc.analytics.application.service.ZoneStatsService;
import com.upc.acusticupc.analytics.domain.dto.AlertsSummaryDTO;
import com.upc.acusticupc.analytics.domain.dto.DashboardKpisDTO;
import com.upc.acusticupc.compliance.domain.model.AlertSeverity;
import com.upc.acusticupc.compliance.domain.model.ComplianceStatus;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.OffsetDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class AnalyticsControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private KpiService kpiService;

    @MockitoBean
    private ZoneStatsService zoneStatsService;

    @MockitoBean
    private AlertStatsService alertStatsService;

    @MockitoBean
    private ZoneDetailService zoneDetailService;

    private MockMvc mockMvc;

    @PostConstruct
    void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    void kpis_sinToken_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/kpis"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void kpis_conAdmin_returns200_yEstructuraValida() throws Exception {
        var bySev = new EnumMap<AlertSeverity, Long>(AlertSeverity.class);
        bySev.put(AlertSeverity.CRITICA, 2L);
        bySev.put(AlertSeverity.MODERADA, 5L);
        bySev.put(AlertSeverity.LEVE, 1L);

        when(kpiService.computeKpis(any(), any(), any(), any())).thenReturn(
                new DashboardKpisDTO(
                        1000L, 3, 8L, bySev, 85.5,
                        OffsetDateTime.parse("2026-04-01T00:00:00-05:00"),
                        OffsetDateTime.parse("2026-04-30T23:59:59-05:00")
                )
        );

        mockMvc.perform(get("/api/v1/analytics/kpis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalMeasurements").value(1000))
                .andExpect(jsonPath("$.zonesActive").value(3))
                .andExpect(jsonPath("$.alertsActive").value(8))
                .andExpect(jsonPath("$.complianceRatePercent").value(85.5));
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void kpis_conViewer_returns200() throws Exception {
        when(kpiService.computeKpis(any(), any(), any(), any())).thenReturn(
                new DashboardKpisDTO(
                        0L, 0, 0L, new EnumMap<>(AlertSeverity.class), 0.0,
                        OffsetDateTime.now(), OffsetDateTime.now()
                )
        );

        mockMvc.perform(get("/api/v1/analytics/kpis"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void zonesStats_conAdmin_returns200_yListaConTodasLasZonas() throws Exception {
        when(zoneStatsService.statsForAllZones(any(), any())).thenReturn(List.of(
                new com.upc.acusticupc.analytics.domain.dto.ZoneStatsDTO(
                        UUID.randomUUID(), "Bloque A", "B_TRANQUILIDAD_RUIDO_MODERADO",
                        "UNIVERSIDAD", 100L, 62.5, 48.3, 65.0, 55.0, 0L,
                        ComplianceStatus.CUMPLE
                ),
                new com.upc.acusticupc.analytics.domain.dto.ZoneStatsDTO(
                        UUID.randomUUID(), "Bloque B", "B_TRANQUILIDAD_RUIDO_MODERADO",
                        "UNIVERSIDAD", 200L, 70.0, 60.0, 65.0, 55.0, 2L,
                        ComplianceStatus.NO_CUMPLE
                )
        ));

        mockMvc.perform(get("/api/v1/analytics/zones/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].zoneName").value("Bloque A"))
                .andExpect(jsonPath("$[1].overallStatus").value("NO_CUMPLE"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void zoneDetail_zonaInexistente_propagaError() throws Exception {
        when(zoneDetailService.detail(any(), any(), any()))
                .thenThrow(new IllegalArgumentException("Zona no encontrada"));

        // Sprint 5 (Dev 2, D4): GlobalExceptionHandler ahora mapea IllegalArgumentException a 400.
        mockMvc.perform(get("/api/v1/analytics/zones/" + UUID.randomUUID()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void alertsSummary_conAdmin_returns200_estructuraValida() throws Exception {
        var bySev = new EnumMap<AlertSeverity, Long>(AlertSeverity.class);
        bySev.put(AlertSeverity.CRITICA, 1L);

        when(alertStatsService.summary(any(), any())).thenReturn(
                new AlertsSummaryDTO(1L, bySev, java.util.Map.of("Bloque A", 1L), List.of())
        );

        mockMvc.perform(get("/api/v1/analytics/alerts/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1));
    }
}