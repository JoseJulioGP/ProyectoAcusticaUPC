package com.upc.acusticupc.ingestion.infrastructure.web;

import com.upc.acusticupc.ingestion.application.service.FolderService;
import com.upc.acusticupc.ingestion.infrastructure.web.dto.FolderResponse;
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
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class FolderControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private FolderService folderService;

    private MockMvc mockMvc;

    @PostConstruct
    void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    private FolderResponse sample(UUID id) {
        return new FolderResponse(id, "Sin clasificar", null, OffsetDateTime.now());
    }

    private static final String BODY = "{\"name\":\"Campaña 2026\",\"parentId\":null}";

    // ---- GET ----

    @Test
    @WithMockUser(roles = "VIEWER")
    void list_autenticado_retorna200() throws Exception {
        when(folderService.list()).thenReturn(List.of(sample(UUID.randomUUID())));
        mockMvc.perform(get("/api/v1/folders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Sin clasificar"));
    }

    @Test
    void list_sinToken_retorna401() throws Exception {
        mockMvc.perform(get("/api/v1/folders"))
                .andExpect(status().isUnauthorized());
    }

    // ---- POST ----

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_conAdmin_retorna201() throws Exception {
        when(folderService.create(any(), anyString())).thenReturn(sample(UUID.randomUUID()));
        mockMvc.perform(post("/api/v1/folders")
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ANALYST")
    void create_conAnalyst_retorna201() throws Exception {
        when(folderService.create(any(), anyString())).thenReturn(sample(UUID.randomUUID()));
        mockMvc.perform(post("/api/v1/folders")
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void create_conViewer_retorna403() throws Exception {
        mockMvc.perform(post("/api/v1/folders")
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isForbidden());
    }

    // ---- PUT ----

    @Test
    @WithMockUser(roles = "ANALYST")
    void update_conAnalyst_retorna200() throws Exception {
        UUID id = UUID.randomUUID();
        when(folderService.update(eq(id), any())).thenReturn(sample(id));
        mockMvc.perform(put("/api/v1/folders/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void update_conViewer_retorna403() throws Exception {
        mockMvc.perform(put("/api/v1/folders/{id}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isForbidden());
    }

    // ---- DELETE ----

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_conAdmin_retorna204() throws Exception {
        mockMvc.perform(delete("/api/v1/folders/{id}", UUID.randomUUID()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_conSubcarpetas_retorna409_folderHasChildren() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new IllegalStateException("FOLDER_HAS_CHILDREN: tiene subcarpetas"))
                .when(folderService).delete(id);

        mockMvc.perform(delete("/api/v1/folders/{id}", id))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("FOLDER_HAS_CHILDREN")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_conBatches_retorna409_folderInUse() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new IllegalStateException("FOLDER_IN_USE: tiene batches asociados"))
                .when(folderService).delete(id);

        mockMvc.perform(delete("/api/v1/folders/{id}", id))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("FOLDER_IN_USE")));
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void delete_conViewer_retorna403() throws Exception {
        mockMvc.perform(delete("/api/v1/folders/{id}", UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }
}
