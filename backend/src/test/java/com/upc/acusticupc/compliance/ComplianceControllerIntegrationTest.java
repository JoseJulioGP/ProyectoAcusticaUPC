package com.upc.acusticupc.compliance;

import com.upc.acusticupc.compliance.application.service.ComplianceEngine;
import com.upc.acusticupc.compliance.domain.repository.AlertRepository;
import com.upc.acusticupc.compliance.domain.repository.ComplianceResultRepository;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class ComplianceControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private ComplianceEngine engine;

    @MockitoBean
    private ComplianceResultRepository resultRepository;

    @MockitoBean
    private AlertRepository alertRepository;

    private MockMvc mockMvc;

    @PostConstruct
    void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void admin_puedeDispararEvaluacion_devuelve202() throws Exception {
        UUID batchId = UUID.randomUUID();
        mockMvc.perform(post("/api/v1/compliance/evaluate/" + batchId))
                .andExpect(status().isAccepted());
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void viewer_noPuedeDispararEvaluacion_devuelve403() throws Exception {
        UUID batchId = UUID.randomUUID();
        mockMvc.perform(post("/api/v1/compliance/evaluate/" + batchId))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void viewer_puedeLeerAlertas_devuelve200() throws Exception {
        when(alertRepository.findByTriggeredAtBetween(any(), any(), any()))
                .thenReturn(Page.empty());

        mockMvc.perform(get("/api/v1/compliance/alerts")
                        .param("from", "2026-01-01T00:00:00-05:00")
                        .param("to", "2026-12-31T23:59:59-05:00"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ANALYST")
    void analyst_puedeLeerAlertas_devuelve200() throws Exception {
        // Sprint 8: ANALYST ya no queda fuera de compliance (antes daba 403).
        when(alertRepository.findByTriggeredAtBetween(any(), any(), any()))
                .thenReturn(Page.empty());

        mockMvc.perform(get("/api/v1/compliance/alerts")
                        .param("from", "2026-01-01T00:00:00-05:00")
                        .param("to", "2026-12-31T23:59:59-05:00"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ANALYST")
    void analyst_puedeLeerResultados_devuelve200() throws Exception {
        when(resultRepository.findByZoneIdAndEvaluatedAtBetween(any(), any(), any(), any()))
                .thenReturn(Page.empty());

        mockMvc.perform(get("/api/v1/compliance/results")
                        .param("zoneId", UUID.randomUUID().toString())
                        .param("from", "2026-01-01T00:00:00-05:00")
                        .param("to", "2026-12-31T23:59:59-05:00"))
                .andExpect(status().isOk());
    }

    @Test
    void sinToken_devuelve401() throws Exception {
        mockMvc.perform(get("/api/v1/compliance/alerts")
                        .param("from", "2026-01-01T00:00:00-05:00")
                        .param("to", "2026-12-31T23:59:59-05:00"))
                .andExpect(status().isUnauthorized());
    }
}
