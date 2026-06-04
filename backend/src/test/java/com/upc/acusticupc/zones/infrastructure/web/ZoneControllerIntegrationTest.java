package com.upc.acusticupc.zones.infrastructure.web;

import com.upc.acusticupc.zones.application.ZoneService;
import com.upc.acusticupc.zones.domain.model.Sector;
import com.upc.acusticupc.zones.domain.model.Subsector;
import com.upc.acusticupc.zones.infrastructure.web.dto.ZoneResponse;
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
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class ZoneControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private ZoneService zoneService;

    private MockMvc mockMvc;

    @PostConstruct
    void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    private ZoneResponse sample(UUID id) {
        return new ZoneResponse(id, "Bloque Administrativo", "desc", "Bloque A", "1",
                Sector.B_TRANQUILIDAD_RUIDO_MODERADO, Subsector.UNIVERSIDADES_COLEGIOS,
                true, 10.0, 20.0, 5.0, 5.0, OffsetDateTime.now());
    }

    private static final String BODY = """
            {"name":"Bloque Administrativo","sector":"B_TRANQUILIDAD_RUIDO_MODERADO",
             "subsector":"UNIVERSIDADES_COLEGIOS"}""";

    // ---- GET ----

    @Test
    @WithMockUser(roles = "VIEWER")
    void list_autenticado_retorna200() throws Exception {
        when(zoneService.list()).thenReturn(java.util.List.of(sample(UUID.randomUUID())));
        mockMvc.perform(get("/api/v1/zones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Bloque Administrativo"));
    }

    @Test
    void list_sinToken_retorna401() throws Exception {
        mockMvc.perform(get("/api/v1/zones")).andExpect(status().isUnauthorized());
    }

    // ---- POST / PUT ----

    @Test
    @WithMockUser(roles = "ANALYST")
    void create_conAnalyst_retorna201() throws Exception {
        when(zoneService.create(any())).thenReturn(sample(UUID.randomUUID()));
        mockMvc.perform(post("/api/v1/zones")
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void create_conViewer_retorna403() throws Exception {
        mockMvc.perform(post("/api/v1/zones")
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void update_conAdmin_retorna200() throws Exception {
        UUID id = UUID.randomUUID();
        when(zoneService.update(eq(id), any())).thenReturn(sample(id));
        mockMvc.perform(put("/api/v1/zones/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk());
    }

    // ---- DELETE ----

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_porDefecto_esSoftDelete_retorna204() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(delete("/api/v1/zones/{id}", id))
                .andExpect(status().isNoContent());
        verify(zoneService).delete(id, false);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_hardSinBatches_retorna204() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(delete("/api/v1/zones/{id}", id).param("hard", "true"))
                .andExpect(status().isNoContent());
        verify(zoneService).delete(id, true);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_hardConBatches_retorna409_zoneInUse() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new IllegalStateException("ZONE_IN_USE: tiene batches asociados"))
                .when(zoneService).delete(id, true);

        mockMvc.perform(delete("/api/v1/zones/{id}", id).param("hard", "true"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("ZONE_IN_USE")));
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void delete_conViewer_retorna403() throws Exception {
        mockMvc.perform(delete("/api/v1/zones/{id}", UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }
}
