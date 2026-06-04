package com.upc.acusticupc.auth.infrastructure.security;

import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Sprint 7 — Bloque B · Tests del {@link RateLimitFilter}.
 *
 * <p>Cada test usa una IP única para evitar contaminación entre métodos (los buckets
 * viven en el filtro, que es singleton dentro del contexto de Spring).</p>
 */
@SpringBootTest
class RateLimitFilterIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @PostConstruct
    void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    /** Forzar IP del cliente en el MockHttpServletRequest. */
    private static RequestPostProcessor remoteAddr(String ip) {
        return req -> { req.setRemoteAddr(ip); return req; };
    }

    private MockHttpServletRequestBuilder postEmpty(String path, String ip) {
        return post(path)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
                .with(remoteAddr(ip));
    }

    @Test
    void register_excedeLimite_devuelve429() throws Exception {
        String ip = "10.20.30.41"; // IP única para este test
        // Las 5 primeras peticiones consumen el bucket. Body vacío → 400 por validación,
        // pero el filtro ya las contó (lo que importa es que pasan por el filtro).
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(postEmpty("/api/v1/auth/register", ip))
                    .andExpect(status().isBadRequest());
        }
        // La 6ª: rate limited.
        mockMvc.perform(postEmpty("/api/v1/auth/register", ip))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("RATE_LIMITED"))
                .andExpect(jsonPath("$.message").value("Demasiados intentos"));
    }

    @Test
    void login_excedeLimite_devuelve429() throws Exception {
        String ip = "10.20.30.42"; // IP única para este test
        // 10 peticiones consumen el bucket; body vacío → 400 por validación.
        for (int i = 0; i < 10; i++) {
            mockMvc.perform(postEmpty("/api/v1/auth/login", ip))
                    .andExpect(status().isBadRequest());
        }
        // La 11ª: rate limited.
        mockMvc.perform(postEmpty("/api/v1/auth/login", ip))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("RATE_LIMITED"));
    }

    @Test
    void rateLimit_porIPSeparado_noSeMezcla() throws Exception {
        String ipA = "10.20.30.51";
        String ipB = "10.20.30.52";
        // IP A agota su cupo de register.
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(postEmpty("/api/v1/auth/register", ipA))
                    .andExpect(status().isBadRequest());
        }
        mockMvc.perform(postEmpty("/api/v1/auth/register", ipA))
                .andExpect(status().isTooManyRequests());

        // IP B sigue libre — su bucket es independiente.
        mockMvc.perform(postEmpty("/api/v1/auth/register", ipB))
                .andExpect(status().isBadRequest());
    }
}
