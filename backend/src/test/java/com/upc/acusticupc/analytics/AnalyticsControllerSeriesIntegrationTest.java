package com.upc.acusticupc.analytics;

import com.upc.acusticupc.analytics.application.service.HeatmapService;
import com.upc.acusticupc.analytics.application.service.TimeSeriesService;
import com.upc.acusticupc.analytics.application.service.ZoneComparisonService;
import com.upc.acusticupc.analytics.domain.dto.HeatmapDTO;
import com.upc.acusticupc.analytics.domain.dto.Granularity;
import com.upc.acusticupc.analytics.domain.dto.TimeSeriesPointDTO;
import com.upc.acusticupc.analytics.domain.dto.ZoneComparisonDTO;
import com.upc.acusticupc.sonometry.domain.model.Period;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class AnalyticsControllerSeriesIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private TimeSeriesService timeSeriesService;

    @MockitoBean
    private HeatmapService heatmapService;

    @MockitoBean
    private ZoneComparisonService zoneComparisonService;

    private MockMvc mockMvc;

    @PostConstruct
    void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    private TimeSeriesPointDTO samplePoint() {
        return new TimeSeriesPointDTO(
                OffsetDateTime.parse("2026-04-01T10:00:00-05:00"),
                UUID.randomUUID(), "Bloque A", 63.5, 100L, Period.DIURNO);
    }

    // ---------- timeseries ----------

    @Test
    void timeseries_sinToken_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/timeseries"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void timeseries_conAdmin_returns200_estructuraOK() throws Exception {
        when(timeSeriesService.series(any(), any(), any(), any(), any()))
                .thenReturn(List.of(samplePoint()));

        mockMvc.perform(get("/api/v1/analytics/timeseries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].zoneName").value("Bloque A"))
                .andExpect(jsonPath("$[0].sampleCount").value(100))
                .andExpect(jsonPath("$[0].period").value("DIURNO"))
                .andExpect(jsonPath("$[0].laeqDb", closeTo(63.5, 0.001)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void timeseries_conGranularityDAY_devuelveBucketsConDateTruncDia() throws Exception {
        when(timeSeriesService.series(any(), any(), any(), any(), any()))
                .thenReturn(List.of(samplePoint()));

        mockMvc.perform(get("/api/v1/analytics/timeseries").param("granularity", "DAY"))
                .andExpect(status().isOk());

        ArgumentCaptor<Granularity> cap = ArgumentCaptor.forClass(Granularity.class);
        verify(timeSeriesService).series(any(), any(), any(), any(), cap.capture());
        assertThat(cap.getValue()).isEqualTo(Granularity.DAY);
    }

    // ---------- heatmap ----------

    @Test
    @WithMockUser(roles = "ADMIN")
    void heatmap_conAdmin_returns200_matrizCorrectaDimensiones() throws Exception {
        HeatmapDTO dto = new HeatmapDTO(
                List.of("Bloque A", "Bloque B"),
                IntStream.range(0, 24).boxed().toList(),
                new double[2][24],
                new double[2][24]);
        when(heatmapService.buildHeatmap(any(), any(), any())).thenReturn(dto);

        mockMvc.perform(get("/api/v1/analytics/heatmap"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.zoneNames.length()").value(2))
                .andExpect(jsonPath("$.laeqMatrix.length()").value(2))
                .andExpect(jsonPath("$.laeqMatrix[0].length()").value(24));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void heatmap_sinDatos_devuelveMatrizDeUnos() throws Exception {
        double[][] laeq = new double[1][24];
        double[][] excess = new double[1][24];
        Arrays.fill(laeq[0], -1.0);
        Arrays.fill(excess[0], -1.0);
        HeatmapDTO dto = new HeatmapDTO(
                List.of("Bloque A"), IntStream.range(0, 24).boxed().toList(), laeq, excess);
        when(heatmapService.buildHeatmap(any(), any(), any())).thenReturn(dto);

        mockMvc.perform(get("/api/v1/analytics/heatmap"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.laeqMatrix[0][0]", closeTo(-1.0, 0.001)))
                .andExpect(jsonPath("$.laeqMatrix[0][23]", closeTo(-1.0, 0.001)));
    }

    // ---------- comparison ----------

    @Test
    @WithMockUser(roles = "ADMIN")
    void comparison_conZoneIdsExplicitos_devuelveSoloEsasZonas() throws Exception {
        UUID z1 = UUID.randomUUID();
        UUID z2 = UUID.randomUUID();
        when(zoneComparisonService.compare(any(), any(), any(), any()))
                .thenReturn(List.of(
                        new ZoneComparisonDTO(z1, "Bloque A", "B", "UNIVERSIDAD",
                                63.5, 48.0, 65.0, 50.0, -1.5, -2.0),
                        new ZoneComparisonDTO(z2, "Bloque B", "B", "UNIVERSIDAD",
                                70.0, 55.0, 65.0, 50.0, 5.0, 5.0)));

        mockMvc.perform(get("/api/v1/analytics/comparison")
                        .param("zoneIds", z1.toString(), z2.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<UUID>> cap = ArgumentCaptor.forClass(List.class);
        verify(zoneComparisonService).compare(any(), any(), cap.capture(), any());
        assertThat(cap.getValue()).containsExactly(z1, z2);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void comparison_sinZoneIds_devuelveTodasLasZonas() throws Exception {
        when(zoneComparisonService.compare(any(), any(), any(), any()))
                .thenReturn(List.of(
                        new ZoneComparisonDTO(UUID.randomUUID(), "A", "B", "U", 60.0, 45.0, 65.0, 50.0, -5.0, -5.0),
                        new ZoneComparisonDTO(UUID.randomUUID(), "B", "B", "U", 61.0, 46.0, 65.0, 50.0, -4.0, -4.0),
                        new ZoneComparisonDTO(UUID.randomUUID(), "C", "B", "U", 62.0, 47.0, 65.0, 50.0, -3.0, -3.0)));

        mockMvc.perform(get("/api/v1/analytics/comparison"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void comparison_conPeriodoDIURNO_nocturnoLaeqEsNull() throws Exception {
        when(zoneComparisonService.compare(any(), any(), any(), any()))
                .thenReturn(List.of(
                        new ZoneComparisonDTO(UUID.randomUUID(), "Bloque A", "B", "UNIVERSIDAD",
                                63.5, null, 65.0, 50.0, -1.5, null)));

        mockMvc.perform(get("/api/v1/analytics/comparison").param("period", "DIURNO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].laeqDiurnoDb", closeTo(63.5, 0.001)))
                .andExpect(jsonPath("$[0].laeqNocturnoDb", nullValue()));
    }
}
