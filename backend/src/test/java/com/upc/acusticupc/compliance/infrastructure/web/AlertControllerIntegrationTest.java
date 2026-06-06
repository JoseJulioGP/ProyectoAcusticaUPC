package com.upc.acusticupc.compliance.infrastructure.web;

import com.upc.acusticupc.compliance.application.dto.AlertResponse;
import com.upc.acusticupc.compliance.application.service.AlertService;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class AlertControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private AlertService alertService;

    private MockMvc mockMvc;

    @PostConstruct
    void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    private AlertResponse sample() {
        return new AlertResponse(UUID.randomUUID(), UUID.randomUUID(), "Bloque A",
                OffsetDateTime.parse("2026-04-01T22:00:00-05:00"), "NOCTURNO",
                72.5, 50.0, 22.5, "CRITICA", null);
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void list_autenticado_retorna200_conContenido() throws Exception {
        when(alertService.findAlerts(any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(sample())));

        mockMvc.perform(get("/api/v1/alerts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].zoneName").value("Bloque A"))
                .andExpect(jsonPath("$.content[0].severity").value("CRITICA"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void list_conFiltros_retorna200() throws Exception {
        when(alertService.findAlerts(any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(sample())));

        mockMvc.perform(get("/api/v1/alerts")
                        .param("zoneId", UUID.randomUUID().toString())
                        .param("severity", "CRITICA")
                        .param("from", "2026-04-01T00:00:00-05:00")
                        .param("to", "2026-04-30T23:59:59-05:00"))
                .andExpect(status().isOk());
    }

    @Test
    void list_sinToken_retorna401() throws Exception {
        mockMvc.perform(get("/api/v1/alerts")).andExpect(status().isUnauthorized());
    }
}
