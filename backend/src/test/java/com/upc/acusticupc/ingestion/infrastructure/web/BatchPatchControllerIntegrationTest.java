package com.upc.acusticupc.ingestion.infrastructure.web;

import com.upc.acusticupc.ingestion.application.service.BatchManagementService;
import com.upc.acusticupc.ingestion.application.service.BatchQueryService;
import com.upc.acusticupc.ingestion.application.service.SonometerIngestionService;
import com.upc.acusticupc.shared.exception.ResourceNotFoundException;
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

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ITs de los endpoints PATCH de Sprint 7: observación y carpeta del batch.
 * Mockea la capa de servicio (patrón del resto de ITs de controller).
 */
@SpringBootTest
class BatchPatchControllerIntegrationTest {

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

    // ---- PATCH observation ----

    @Test
    @WithMockUser(roles = "ANALYST")
    void observation_conAnalyst_retorna204() throws Exception {
        mockMvc.perform(patch("/api/v1/ingest/batches/{id}/observation", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"observation\":\"medición nocturna atípica\"}"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void observation_conViewer_retorna403() throws Exception {
        mockMvc.perform(patch("/api/v1/ingest/batches/{id}/observation", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"observation\":\"x\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void observation_sinToken_retorna401() throws Exception {
        mockMvc.perform(patch("/api/v1/ingest/batches/{id}/observation", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"observation\":\"x\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void observation_batchInexistente_retorna404() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new ResourceNotFoundException("Batch", id))
                .when(managementService).updateObservation(eq(id), any());

        mockMvc.perform(patch("/api/v1/ingest/batches/{id}/observation", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"observation\":\"x\"}"))
                .andExpect(status().isNotFound());
    }

    // ---- PATCH folder ----

    @Test
    @WithMockUser(roles = "ANALYST")
    void folder_conAnalyst_retorna204() throws Exception {
        mockMvc.perform(patch("/api/v1/ingest/batches/{id}/folder", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"folderId\":\"" + UUID.randomUUID() + "\"}"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void folder_conViewer_retorna403() throws Exception {
        mockMvc.perform(patch("/api/v1/ingest/batches/{id}/folder", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"folderId\":null}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void folder_batchInexistente_retorna404() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new ResourceNotFoundException("Batch", id))
                .when(managementService).moveToFolder(eq(id), any());

        mockMvc.perform(patch("/api/v1/ingest/batches/{id}/folder", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"folderId\":null}"))
                .andExpect(status().isNotFound());
    }
}
