package com.upc.acusticupc.ingestion.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.upc.acusticupc.ingestion.application.service.BatchManagementService;
import com.upc.acusticupc.ingestion.application.service.BatchQueryService;
import com.upc.acusticupc.ingestion.application.service.SonometerIngestionService;
import com.upc.acusticupc.sonometry.domain.model.BatchStatus;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class BatchManagementControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private SonometerIngestionService ingestionService;

    @MockitoBean
    private BatchQueryService queryService;

    @MockitoBean
    private BatchManagementService managementService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @PostConstruct
    void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    // ---- retry ----

    @Test
    @WithMockUser(roles = "ADMIN")
    void retry_conAdmin_retorna200() throws Exception {
        UUID id = UUID.randomUUID();
        when(managementService.retry(any())).thenReturn(fakeBatch(id, BatchStatus.PENDING));

        mockMvc.perform(post("/api/v1/ingest/batches/{id}/retry", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void retry_conViewer_retorna403() throws Exception {
        mockMvc.perform(post("/api/v1/ingest/batches/{id}/retry", UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ANALYST")
    void retry_conAnalyst_retorna403() throws Exception {
        mockMvc.perform(post("/api/v1/ingest/batches/{id}/retry", UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }

    // ---- markFailed ----

    @Test
    @WithMockUser(roles = "ADMIN")
    void markFailed_conAdmin_retorna200() throws Exception {
        UUID id = UUID.randomUUID();
        when(managementService.markFailed(any(), any()))
                .thenReturn(fakeBatch(id, BatchStatus.FAILED));

        String body = objectMapper.writeValueAsString(Map.of("reason", "prueba manual"));

        mockMvc.perform(post("/api/v1/ingest/batches/{id}/fail", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FAILED"));
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void markFailed_conViewer_retorna403() throws Exception {
        mockMvc.perform(post("/api/v1/ingest/batches/{id}/fail", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ANALYST")
    void markFailed_conAnalyst_retorna403() throws Exception {
        mockMvc.perform(post("/api/v1/ingest/batches/{id}/fail", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    // ---- delete ----

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_conAdmin_retorna204() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(managementService).delete(id);

        mockMvc.perform(delete("/api/v1/ingest/batches/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void delete_conViewer_retorna403() throws Exception {
        mockMvc.perform(delete("/api/v1/ingest/batches/{id}", UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ANALYST")
    void delete_conAnalyst_retorna403() throws Exception {
        mockMvc.perform(delete("/api/v1/ingest/batches/{id}", UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }

    // ---- helpers ----

    private com.upc.acusticupc.sonometry.domain.model.MeasurementBatch fakeBatch(
            UUID id, BatchStatus status) {
        var b = new com.upc.acusticupc.sonometry.domain.model.MeasurementBatch();
        b.setId(id);
        b.setFileName("test.xlsx");
        b.setStatus(status);
        b.setUploadedAt(OffsetDateTime.now());
        return b;
    }

}
