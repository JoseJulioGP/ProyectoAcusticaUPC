package com.upc.acusticupc.auth.infrastructure.security;

import jakarta.annotation.PostConstruct;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/**
 * Sprint 7 — Bloque B · Matriz rol × endpoint.
 *
 * <p>Para cada combinación (ADMIN / ANALYST / VIEWER / ANON) y endpoint relevante
 * del Sprint 7 se verifica:</p>
 * <ul>
 *   <li><b>401</b> cuando no hay token.</li>
 *   <li><b>403</b> cuando el rol no tiene permiso.</li>
 *   <li><b>OK</b> = no-401-y-no-403 cuando el rol pasa autorización (el controller
 *       puede responder 200/201/204/400/404 según la lógica de negocio; la matriz
 *       solo verifica que la autorización dejó pasar).</li>
 * </ul>
 *
 * <p>Importante: los bodies/params se envían VÁLIDOS para que la resolución de
 * argumentos (@Valid, @RequestParam required) NO produzca un 400 antes de que
 * Spring evalúe el @PreAuthorize. Si esto no se respetara, los roles
 * insuficientes verían 400 por validación en vez del 403 esperado.</p>
 */
@SpringBootTest
class RoleEndpointAccessMatrixIntegrationTest {

    private static final String SAMPLE_UUID = "11111111-1111-1111-1111-111111111111";

    // Bodies válidos por endpoint (pasan @Valid y permiten que @PreAuthorize evalúe el rol).
    private static final String ZONE_BODY = """
            {"name":"Zona Matriz","sector":"B_TRANQUILIDAD_RUIDO_MODERADO","subsector":"UNIVERSIDADES_COLEGIOS"}
            """;
    private static final String FOLDER_BODY = """
            {"name":"Carpeta Matriz"}
            """;
    private static final String BATCH_OBS_BODY = """
            {"observation":"nota arbitraria"}
            """;
    private static final String BATCH_FOLDER_BODY = """
            {"folderId":null}
            """;

    // Query strings con TODOS los params requeridos por el endpoint.
    private static final String ANALYTICS_WEEKDAY_QS  = "?year=2026&isoWeekFrom=1&isoWeekTo=2";
    private static final String ANALYTICS_BEFORE_QS   = "?pivot=2026-01-01T00:00:00-05:00";
    private static final String REPORT_XLSX_QS        = "?from=2026-01-01&to=2026-01-31";

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @PostConstruct
    void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @ParameterizedTest(name = "[{index}] {2} {0} {1} -> {3}")
    @CsvSource(delimiter = '|', textBlock = """
            # === /api/v1/zones (GET isAuth | POST,PUT ADMIN+ANALYST | DELETE ADMIN) ===
            GET    | /api/v1/zones                                          | ADMIN    | OK
            GET    | /api/v1/zones                                          | ANALYST  | OK
            GET    | /api/v1/zones                                          | VIEWER   | OK
            GET    | /api/v1/zones                                          | ANON     | 401
            POST   | /api/v1/zones                                          | ADMIN    | OK
            POST   | /api/v1/zones                                          | ANALYST  | OK
            POST   | /api/v1/zones                                          | VIEWER   | 403
            POST   | /api/v1/zones                                          | ANON     | 401
            PUT    | /api/v1/zones/{id}                                     | ADMIN    | OK
            PUT    | /api/v1/zones/{id}                                     | ANALYST  | OK
            PUT    | /api/v1/zones/{id}                                     | VIEWER   | 403
            PUT    | /api/v1/zones/{id}                                     | ANON     | 401
            DELETE | /api/v1/zones/{id}                                     | ADMIN    | OK
            DELETE | /api/v1/zones/{id}                                     | ANALYST  | 403
            DELETE | /api/v1/zones/{id}                                     | VIEWER   | 403
            DELETE | /api/v1/zones/{id}                                     | ANON     | 401

            # === /api/v1/folders (mismo patrón que /zones) ===
            GET    | /api/v1/folders                                        | ADMIN    | OK
            GET    | /api/v1/folders                                        | ANALYST  | OK
            GET    | /api/v1/folders                                        | VIEWER   | OK
            GET    | /api/v1/folders                                        | ANON     | 401
            POST   | /api/v1/folders                                        | ADMIN    | OK
            POST   | /api/v1/folders                                        | ANALYST  | OK
            POST   | /api/v1/folders                                        | VIEWER   | 403
            POST   | /api/v1/folders                                        | ANON     | 401
            PUT    | /api/v1/folders/{id}                                   | ADMIN    | OK
            PUT    | /api/v1/folders/{id}                                   | ANALYST  | OK
            PUT    | /api/v1/folders/{id}                                   | VIEWER   | 403
            PUT    | /api/v1/folders/{id}                                   | ANON     | 401
            DELETE | /api/v1/folders/{id}                                   | ADMIN    | OK
            DELETE | /api/v1/folders/{id}                                   | ANALYST  | 403
            DELETE | /api/v1/folders/{id}                                   | VIEWER   | 403
            DELETE | /api/v1/folders/{id}                                   | ANON     | 401

            # === PATCH /ingest/batches/{id}/observation y /folder (ADMIN+ANALYST) ===
            PATCH  | /api/v1/ingest/batches/{id}/observation                | ADMIN    | OK
            PATCH  | /api/v1/ingest/batches/{id}/observation                | ANALYST  | OK
            PATCH  | /api/v1/ingest/batches/{id}/observation                | VIEWER   | 403
            PATCH  | /api/v1/ingest/batches/{id}/observation                | ANON     | 401
            PATCH  | /api/v1/ingest/batches/{id}/folder                     | ADMIN    | OK
            PATCH  | /api/v1/ingest/batches/{id}/folder                     | ANALYST  | OK
            PATCH  | /api/v1/ingest/batches/{id}/folder                     | VIEWER   | 403
            PATCH  | /api/v1/ingest/batches/{id}/folder                     | ANON     | 401

            # === GET /alerts (isAuthenticated) ===
            GET    | /api/v1/alerts                                         | ADMIN    | OK
            GET    | /api/v1/alerts                                         | ANALYST  | OK
            GET    | /api/v1/alerts                                         | VIEWER   | OK
            GET    | /api/v1/alerts                                         | ANON     | 401

            # === /analytics (Sprint 8: class-level hasAnyRole ADMIN,ANALYST,VIEWER -> ANALYST OK) ===
            GET    | /api/v1/analytics/weekday                              | ADMIN    | OK
            GET    | /api/v1/analytics/weekday                              | ANALYST  | OK
            GET    | /api/v1/analytics/weekday                              | VIEWER   | OK
            GET    | /api/v1/analytics/weekday                              | ANON     | 401
            GET    | /api/v1/analytics/before-after                         | ADMIN    | OK
            GET    | /api/v1/analytics/before-after                         | ANALYST  | OK
            GET    | /api/v1/analytics/before-after                         | VIEWER   | OK
            GET    | /api/v1/analytics/before-after                         | ANON     | 401

            # === GET /reports/dashboard.xlsx (isAuthenticated) ===
            GET    | /api/v1/reports/dashboard.xlsx                         | ADMIN    | OK
            GET    | /api/v1/reports/dashboard.xlsx                         | ANALYST  | OK
            GET    | /api/v1/reports/dashboard.xlsx                         | VIEWER   | OK
            GET    | /api/v1/reports/dashboard.xlsx                         | ANON     | 401
            """)
    void matrixRolXEndpoint(String method, String pathTemplate, String role, String expected) throws Exception {
        String path = pathTemplate.replace("{id}", SAMPLE_UUID) + queryStringFor(pathTemplate);
        MockHttpServletRequestBuilder req = build(method.trim(), path);

        String body = bodyFor(method.trim(), pathTemplate);
        if (body != null) {
            req.contentType(MediaType.APPLICATION_JSON).content(body);
        }
        if (!"ANON".equalsIgnoreCase(role.trim())) {
            req.with(user("matrix-user").roles(role.trim()));
        }

        int status = mockMvc.perform(req).andReturn().getResponse().getStatus();

        switch (expected.trim()) {
            case "401" -> assertEquals(401, status,
                    () -> method + " " + path + " (" + role + ") esperaba 401, fue " + status);
            case "403" -> assertEquals(403, status,
                    () -> method + " " + path + " (" + role + ") esperaba 403, fue " + status);
            case "OK"  -> assertTrue(status != 401 && status != 403,
                    () -> method + " " + path + " (" + role + ") esperaba autorización OK (no 401/403), fue " + status);
            default    -> throw new IllegalArgumentException("expected col debe ser 401|403|OK, vino: " + expected);
        }
    }

    /** Devuelve un body válido para el endpoint o null si el método no lleva body. */
    private static String bodyFor(String method, String pathTemplate) {
        boolean writes = "POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method);
        if (!writes) return null;
        if (pathTemplate.contains("/zones"))                 return ZONE_BODY;
        if (pathTemplate.contains("/folders"))               return FOLDER_BODY;
        if (pathTemplate.endsWith("/observation"))           return BATCH_OBS_BODY;
        if (pathTemplate.endsWith("/folder"))                return BATCH_FOLDER_BODY;
        return "{}";
    }

    /** Devuelve los query params requeridos por ciertos endpoints (vacío si no aplica). */
    private static String queryStringFor(String pathTemplate) {
        if (pathTemplate.endsWith("/analytics/weekday"))       return ANALYTICS_WEEKDAY_QS;
        if (pathTemplate.endsWith("/analytics/before-after"))  return ANALYTICS_BEFORE_QS;
        if (pathTemplate.endsWith("/reports/dashboard.xlsx"))  return REPORT_XLSX_QS;
        return "";
    }

    private static MockHttpServletRequestBuilder build(String method, String path) {
        return switch (method) {
            case "GET"    -> get(path);
            case "POST"   -> post(path);
            case "PUT"    -> put(path);
            case "PATCH"  -> patch(path);
            case "DELETE" -> delete(path);
            default       -> throw new IllegalArgumentException("Método no soportado: " + method);
        };
    }
}
