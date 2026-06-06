package com.upc.acusticupc.shared.exception;

import com.upc.acusticupc.ingestion.application.service.FolderService;
import com.upc.acusticupc.zones.application.ZoneService;
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

import java.util.UUID;

import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Sprint 8 · Verifica que el {@code GlobalExceptionHandler} extrae el código
 * de negocio del prefijo {@code "CODE: message"} y lo expone como
 * {@code ApiError.code}.
 */
@SpringBootTest
class ApiErrorCodeIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private FolderService folderService;

    @MockitoBean
    private ZoneService zoneService;

    private MockMvc mockMvc;

    @PostConstruct
    void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteFolderConBatches_devuelve409_conCodeFolderInUse() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new IllegalStateException(
                "FOLDER_IN_USE: la carpeta tiene batches asociados; reasígnalos a otra carpeta primero"))
                .when(folderService).delete(id);

        mockMvc.perform(delete("/api/v1/folders/" + id))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("FOLDER_IN_USE"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message",
                        org.hamcrest.Matchers.containsString("FOLDER_IN_USE")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteFolderConSubcarpetas_devuelve409_conCodeFolderHasChildren() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new IllegalStateException(
                "FOLDER_HAS_CHILDREN: la carpeta tiene subcarpetas; elimínalas o muévelas primero"))
                .when(folderService).delete(id);

        mockMvc.perform(delete("/api/v1/folders/" + id))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("FOLDER_HAS_CHILDREN"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void illegalStateSinPrefijoCodigo_codeViajaNull() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new IllegalStateException("mensaje sin prefijo de código"))
                .when(folderService).delete(id);

        mockMvc.perform(delete("/api/v1/folders/" + id))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(org.hamcrest.Matchers.nullValue()));
    }
}
