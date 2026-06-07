package com.upc.acusticupc.auth.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.upc.acusticupc.auth.application.dto.ChangeOwnPasswordRequest;
import com.upc.acusticupc.auth.application.service.AuthService;
import com.upc.acusticupc.shared.exception.DomainException;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class AuthControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AuthService authService;

    private MockMvc mockMvc;

    @PostConstruct
    void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    void changePassword_sinToken_returns401() throws Exception {
        String body = objectMapper.writeValueAsString(
                new ChangeOwnPasswordRequest("OldPass123", "NuevaPass123"));

        mockMvc.perform(patch("/api/v1/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user@upc.edu.co")
    void changePassword_conActualCorrecta_returns204() throws Exception {
        String body = objectMapper.writeValueAsString(
                new ChangeOwnPasswordRequest("OldPass123", "NuevaPass123"));

        mockMvc.perform(patch("/api/v1/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isNoContent());

        verify(authService).changePassword(eq("user@upc.edu.co"), eq("OldPass123"), eq("NuevaPass123"));
    }

    @Test
    @WithMockUser(username = "user@upc.edu.co")
    void changePassword_conActualIncorrecta_returns400() throws Exception {
        doThrow(new DomainException("CURRENT_PASSWORD_INVALID: la contraseña actual es incorrecta"))
                .when(authService).changePassword(any(), any(), any());

        String body = objectMapper.writeValueAsString(
                new ChangeOwnPasswordRequest("wrong", "NuevaPass123"));

        mockMvc.perform(patch("/api/v1/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user@upc.edu.co")
    void changePassword_nuevaDebil_returns400_yNoLlamaServicio() throws Exception {
        // "corta" no cumple el patrón (sin mayúscula/dígito, <10 chars) → Bean Validation rechaza.
        String body = objectMapper.writeValueAsString(
                new ChangeOwnPasswordRequest("OldPass123", "corta"));

        mockMvc.perform(patch("/api/v1/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());

        verify(authService, org.mockito.Mockito.never()).changePassword(any(), any(), any());
    }
}
