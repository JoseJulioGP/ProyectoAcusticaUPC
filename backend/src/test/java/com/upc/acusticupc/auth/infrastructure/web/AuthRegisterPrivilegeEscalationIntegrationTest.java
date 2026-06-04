package com.upc.acusticupc.auth.infrastructure.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Sprint 7 — Bloque A · Cierre de escalada de privilegios en {@code POST /auth/register}.
 *
 * <p>Test del bug histórico: antes del Sprint 7, cualquier anónimo podía hacer
 * <pre>
 *   POST /api/v1/auth/register
 *   { "email":"x", "password":"x", "fullName":"x", "role":"ADMIN" }
 * </pre>
 * y quedaba con rol {@code ADMIN}. El servicio confiaba en el {@code role} del body.</p>
 *
 * <p>Este IT recorre el flujo HTTP completo (register → login → /me) enviando
 * deliberadamente {@code "role":"ADMIN"} como JSON crudo (no a través del DTO,
 * cuyo campo ya fue eliminado). Verifica que el usuario resultante queda con
 * rol {@code VIEWER}: Jackson ignora la propiedad desconocida y el servicio
 * tiene {@code .role(Role.VIEWER)} hardcoded.</p>
 */
@SpringBootTest
class AuthRegisterPrivilegeEscalationIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    // ObjectMapper no se autowirea fiable en este contexto de test;
    // instanciar directo basta para parsear la respuesta del login.
    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @PostConstruct
    void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    void registerConRoleAdminEnBody_usuarioQuedaComoVIEWER() throws Exception {
        // UUID en el email evita colisiones entre corridas si la BD no se limpia
        // (el seeder de tests usa email distinto y el ddl-auto es create-drop, pero
        // así también es seguro si alguien cambia ese setting).
        String email = "registro-malicioso-" + UUID.randomUUID() + "@upc.edu.co";
        String password = "PasswordSegura1";

        // 1) POST /auth/register con role=ADMIN inyectado en el body crudo.
        //    Jackson debe ignorarlo (propiedad desconocida tras quitarla del DTO)
        //    y el servicio debe forzar VIEWER.
        String registerBody = ("""
                {
                  "fullName": "Atacante Pruebas",
                  "email": "%s",
                  "password": "%s",
                  "role": "ADMIN"
                }
                """).formatted(email, password);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email.toLowerCase()))
                .andExpect(jsonPath("$.role").value("VIEWER"))
                .andExpect(jsonPath("$.active").value(true));

        // 2) Login para sacar token JWT
        String loginBody = ("""
                {"email":"%s","password":"%s"}
                """).formatted(email, password);

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.user.role").value("VIEWER"))
                .andReturn();

        JsonNode loginJson = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String token = loginJson.get("token").asText();

        // 3) GET /auth/me con el token del usuario recién creado
        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email.toLowerCase()))
                .andExpect(jsonPath("$.role").value("VIEWER"));
    }
}
