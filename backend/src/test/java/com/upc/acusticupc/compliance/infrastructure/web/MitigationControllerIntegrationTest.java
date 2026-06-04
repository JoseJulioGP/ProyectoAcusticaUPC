package com.upc.acusticupc.compliance.infrastructure.web;

import com.upc.acusticupc.compliance.domain.model.MitigationAction;
import com.upc.acusticupc.compliance.domain.repository.MitigationActionRepository;
import com.upc.acusticupc.shared.util.DateRangeUtil;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Sprint 7 — RF#11 · IT del {@code MitigationController} (CRUD + suggest).
 *
 * <p>Cada test parte de un catálogo fresco de 8 acciones activas (T01..T08)
 * sembradas en {@link #seed()} contra H2 — los SQL de la V9 no se ejecutan
 * en tests (Flyway está deshabilitado en {@code src/test/resources/application.yml}
 * y el esquema se crea con {@code ddl-auto: create-drop}).</p>
 */
@SpringBootTest
class MitigationControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private MitigationActionRepository repository;

    private MockMvc mockMvc;

    @PostConstruct
    void setupMockMvc() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @BeforeEach
    void seed() {
        repository.deleteAll();
        OffsetDateTime now = OffsetDateTime.now(DateRangeUtil.BOGOTA);
        for (int i = 1; i <= 8; i++) {
            repository.save(MitigationAction.builder()
                    .code("T%02d".formatted(i))
                    .title("Acción " + i)
                    .description("Descripción " + i)
                    .regulationRef("Res. 0627/2006")
                    .priority(((i - 1) % 5) + 1)         // 1..5 ciclando
                    .estimatedImpactDb(3.0 + i)
                    .active(true)
                    .createdAt(now)
                    .build());
        }
    }

    // ---------- list ----------

    @Test
    void list_sinToken_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/mitigations"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void list_conViewer_returns200_y8Acciones() throws Exception {
        mockMvc.perform(get("/api/v1/mitigations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(8));
    }

    // ---------- suggest ----------

    @ParameterizedTest(name = "[{index}] excessDb={0} -> {1} acciones")
    @CsvSource({
            "3, 3",
            "8, 5",
            "12, 8"
    })
    @WithMockUser(roles = "VIEWER")
    void suggest_porExcessDb_returnsTopN(double excessDb, int expectedSize) throws Exception {
        mockMvc.perform(get("/api/v1/mitigations/suggest").param("excessDb", String.valueOf(excessDb)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(expectedSize));
    }

    // ---------- create ----------

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_conAdmin_returns201_yPersiste() throws Exception {
        String body = """
                {"code":"NEW01","title":"Nueva acción","description":"texto","priority":3}
                """;
        mockMvc.perform(post("/api/v1/mitigations")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("NEW01"))
                .andExpect(jsonPath("$.title").value("Nueva acción"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.createdAt").exists());
        assertTrue(repository.existsByCode("NEW01"));
    }

    @Test
    @WithMockUser(roles = "ANALYST")
    void create_conAnalyst_returns201() throws Exception {
        String body = """
                {"code":"NEW02","title":"X","description":"d","priority":2}
                """;
        mockMvc.perform(post("/api/v1/mitigations")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void create_conViewer_returns403() throws Exception {
        String body = """
                {"code":"NEW03","title":"X","description":"d","priority":2}
                """;
        mockMvc.perform(post("/api/v1/mitigations")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_codeDuplicado_returns409() throws Exception {
        // T01 ya existe por el seed.
        String body = """
                {"code":"T01","title":"Otra","description":"d","priority":2}
                """;
        mockMvc.perform(post("/api/v1/mitigations")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "ANALYST")
    void create_payloadInvalido_returns400() throws Exception {
        // priority fuera de rango (0) viola @Min(1).
        String body = """
                {"code":"BAD","title":"X","description":"d","priority":0}
                """;
        mockMvc.perform(post("/api/v1/mitigations")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    // ---------- update ----------

    @Test
    @WithMockUser(roles = "ADMIN")
    void update_conAdmin_returns200() throws Exception {
        UUID id = repository.findByCode("T01").orElseThrow().getId();
        String body = """
                {"code":"T01","title":"Modificada","description":"nueva","priority":1}
                """;
        mockMvc.perform(put("/api/v1/mitigations/" + id)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Modificada"))
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void update_conViewer_returns403() throws Exception {
        UUID id = repository.findByCode("T01").orElseThrow().getId();
        String body = """
                {"code":"T01","title":"X","description":"d","priority":1}
                """;
        mockMvc.perform(put("/api/v1/mitigations/" + id)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void update_idInexistente_returns404() throws Exception {
        String body = """
                {"code":"NOPE","title":"X","description":"d","priority":2}
                """;
        mockMvc.perform(put("/api/v1/mitigations/" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isNotFound());
    }

    // ---------- deactivate (DELETE: soft-delete) ----------

    @Test
    @WithMockUser(roles = "ADMIN")
    void deactivate_conAdmin_returns204_yNoApareceEnList_peroSigueEnBD() throws Exception {
        UUID id = repository.findByCode("T01").orElseThrow().getId();

        mockMvc.perform(delete("/api/v1/mitigations/" + id))
                .andExpect(status().isNoContent());

        // GET filtra por active=true: la lista pasa de 8 a 7.
        mockMvc.perform(get("/api/v1/mitigations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(7));

        // La fila NO se borra físicamente: persiste con active=false.
        MitigationAction stored = repository.findById(id).orElseThrow();
        assertFalse(stored.getActive(), "DELETE debe ser soft-delete (active=false), no borrado físico");
    }

    @Test
    @WithMockUser(roles = "ANALYST")
    void deactivate_conAnalyst_returns403() throws Exception {
        UUID id = repository.findByCode("T01").orElseThrow().getId();
        mockMvc.perform(delete("/api/v1/mitigations/" + id))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void deactivate_conViewer_returns403() throws Exception {
        UUID id = repository.findByCode("T01").orElseThrow().getId();
        mockMvc.perform(delete("/api/v1/mitigations/" + id))
                .andExpect(status().isForbidden());
    }
}
