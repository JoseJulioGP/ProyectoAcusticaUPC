package com.upc.acusticupc.auth.infrastructure.web;

import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Sprint 7 — Bloque B · Política de contraseña fuerte en {@code /auth/register}.
 *
 * <p>Regla (en {@link com.upc.acusticupc.auth.application.dto.RegisterRequest}):
 * mínimo 10 caracteres, al menos una mayúscula, una minúscula y un dígito.
 * Cualquier violación devuelve 400 con un mensaje claro del validador.</p>
 *
 * <p>Cada test usa una IP única para no chocar con el rate-limit del filtro
 * (5 registros / hora por IP).</p>
 */
@SpringBootTest
class RegisterPasswordPolicyIntegrationTest {

    private static final String STRONG_MSG = "Mínimo 10 caracteres, una mayúscula, una minúscula y un dígito";

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @PostConstruct
    void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    private static RequestPostProcessor remoteAddr(String ip) {
        return req -> { req.setRemoteAddr(ip); return req; };
    }

    private String body(String email, String password) {
        return ("""
                {"fullName":"Usuario Test","email":"%s","password":"%s"}
                """).formatted(email, password);
    }

    private String uniqueEmail() {
        return "policy-" + UUID.randomUUID() + "@upc.edu.co";
    }

    @Test
    void passwordMuyCorto_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(uniqueEmail(), "Aa1xxx"))   // 6 chars
                        .with(remoteAddr("10.30.0.1")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("password")))
                .andExpect(jsonPath("$.message", containsString(STRONG_MSG)));
    }

    @Test
    void passwordSinMayuscula_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(uniqueEmail(), "passwordsegura1"))  // sin mayúscula
                        .with(remoteAddr("10.30.0.2")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString(STRONG_MSG)));
    }

    @Test
    void passwordSinMinuscula_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(uniqueEmail(), "PASSWORDSEGURA1"))  // sin minúscula
                        .with(remoteAddr("10.30.0.3")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString(STRONG_MSG)));
    }

    @Test
    void passwordSinDigito_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(uniqueEmail(), "PasswordSegura"))   // sin dígito
                        .with(remoteAddr("10.30.0.4")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString(STRONG_MSG)));
    }

    @Test
    void passwordFuerte_returns200_yRolViewer() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(uniqueEmail(), "PasswordSegura1"))  // 10+ chars + Aa1
                        .with(remoteAddr("10.30.0.5")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("VIEWER"))
                .andExpect(jsonPath("$.active").value(true));
    }
}
