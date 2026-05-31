package com.upc.acusticupc.ingestion.infrastructure.web;

import com.upc.acusticupc.ingestion.application.service.BatchManagementService;
import com.upc.acusticupc.ingestion.application.service.BatchQueryService;
import com.upc.acusticupc.ingestion.application.service.SonometerIngestionService;
import com.upc.acusticupc.shared.exception.ResourceNotFoundException;
import com.upc.acusticupc.sonometry.domain.model.BatchStatus;
import com.upc.acusticupc.sonometry.domain.model.MeasurementBatch;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class BatchControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private SonometerIngestionService ingestionService;

    @MockitoBean
    private BatchQueryService queryService;

    @MockitoBean
    private BatchManagementService managementService;

    private MockMvc mockMvc;

    @PostConstruct
    void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    private MeasurementBatch sampleBatch(UUID id, BatchStatus status) {
        return MeasurementBatch.builder()
                .id(id)
                .fileName("muestra.xls")
                .status(status)
                .build();
    }

    // ---------- upload (existente) ----------

    @Test
    @WithMockUser(roles = "ADMIN")
    void uploadReturns202WhenAdmin() throws Exception {
        UUID batchId = UUID.randomUUID();
        UUID zoneId = UUID.randomUUID();
        when(ingestionService.registerBatch(any(), eq(zoneId), any()))
                .thenReturn(batchId);

        MockMultipartFile file = new MockMultipartFile(
                "file", "test.xlsx",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                "fake content".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/ingest/batches")
                        .file(file)
                        .param("zoneId", zoneId.toString()))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.batchId").value(batchId.toString()))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void uploadIsForbiddenForNonAdmin() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.xlsx",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                "fake".getBytes()
        );
        mockMvc.perform(multipart("/api/v1/ingest/batches")
                        .file(file)
                        .param("zoneId", UUID.randomUUID().toString()))
                .andExpect(status().isForbidden());
    }

    // ---------- retry ----------

    @Test
    @WithMockUser(roles = "ADMIN")
    void retry_conAdmin_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        when(managementService.retry(id)).thenReturn(sampleBatch(id, BatchStatus.PENDING));

        mockMvc.perform(post("/api/v1/ingest/batches/" + id + "/retry"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void retry_conViewer_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/ingest/batches/" + UUID.randomUUID() + "/retry"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void retry_batchInexistente_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(managementService.retry(id))
                .thenThrow(new ResourceNotFoundException("Batch", id));

        mockMvc.perform(post("/api/v1/ingest/batches/" + id + "/retry"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void retry_batchCompleted_returns409() throws Exception {
        UUID id = UUID.randomUUID();
        when(managementService.retry(id))
                .thenThrow(new IllegalStateException(
                        "Solo se pueden reintentar batches en estado PROCESSING o FAILED, estado actual: COMPLETED"));

        mockMvc.perform(post("/api/v1/ingest/batches/" + id + "/retry"))
                .andExpect(status().isConflict());
    }

    // ---------- markFailed ----------

    @Test
    @WithMockUser(roles = "ADMIN")
    void fail_conAdmin_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        when(managementService.markFailed(eq(id), any()))
                .thenReturn(sampleBatch(id, BatchStatus.FAILED));

        mockMvc.perform(post("/api/v1/ingest/batches/" + id + "/fail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"timeout manual\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FAILED"));
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void fail_conViewer_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/ingest/batches/" + UUID.randomUUID() + "/fail"))
                .andExpect(status().isForbidden());
    }

    // ---------- delete ----------

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_conAdmin_returns204() throws Exception {
        UUID id = UUID.randomUUID();
        // managementService.delete is void; nothing to stub.
        mockMvc.perform(delete("/api/v1/ingest/batches/" + id))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void delete_conViewer_returns403() throws Exception {
        mockMvc.perform(delete("/api/v1/ingest/batches/" + UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }
}
