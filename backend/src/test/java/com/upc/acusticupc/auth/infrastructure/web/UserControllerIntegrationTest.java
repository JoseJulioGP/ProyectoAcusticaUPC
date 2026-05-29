package com.upc.acusticupc.auth.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.upc.acusticupc.auth.application.dto.ChangePasswordRequest;
import com.upc.acusticupc.auth.application.dto.CreateUserRequest;
import com.upc.acusticupc.auth.application.dto.UpdateUserRequest;
import com.upc.acusticupc.auth.application.dto.UserListItemDTO;
import com.upc.acusticupc.auth.application.service.UserManagementService;
import com.upc.acusticupc.auth.application.service.UserManagementService.EmailAlreadyUsedException;
import com.upc.acusticupc.auth.application.service.UserManagementService.LastAdminException;
import com.upc.acusticupc.auth.domain.model.Role;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class UserControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private UserManagementService service;

    private MockMvc mockMvc;

    @PostConstruct
    void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    private UserListItemDTO sample(String fullName, String email, Role role, boolean active) {
        return new UserListItemDTO(
                UUID.randomUUID(), fullName, email, role, active, OffsetDateTime.now());
    }

    // ---------- list ----------

    @Test
    void list_sinToken_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void list_conViewer_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ANALYST")
    void list_conAnalyst_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void list_conAdmin_returns200() throws Exception {
        Page<UserListItemDTO> page = new PageImpl<>(List.of(
                sample("Admin", "admin@upc.edu.co", Role.ADMIN, true)));
        when(service.list(any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].email").value("admin@upc.edu.co"))
                .andExpect(jsonPath("$.content[0].role").value("ADMIN"));
    }

    // ---------- create ----------

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_conAdmin_returns201_yPersiste() throws Exception {
        UserListItemDTO returned = sample("Nuevo Usuario", "nuevo@upc.edu.co", Role.ANALYST, true);
        when(service.create(any())).thenReturn(returned);

        String body = objectMapper.writeValueAsString(
                new CreateUserRequest("Nuevo Usuario", "nuevo@upc.edu.co", Role.ANALYST, "Password123"));

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("nuevo@upc.edu.co"))
                .andExpect(jsonPath("$.role").value("ANALYST"))
                .andExpect(jsonPath("$.active").value(true));

        verify(service).create(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_emailDuplicado_returns409() throws Exception {
        when(service.create(any())).thenThrow(new EmailAlreadyUsedException("dup@upc.edu.co"));

        String body = objectMapper.writeValueAsString(
                new CreateUserRequest("Otro Usuario", "dup@upc.edu.co", Role.VIEWER, "Password123"));

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    // ---------- update ----------

    @Test
    @WithMockUser(roles = "ADMIN")
    void update_degradarUltimoAdmin_returns409() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.update(eq(id), any())).thenThrow(new LastAdminException());

        String body = objectMapper.writeValueAsString(
                new UpdateUserRequest("Admin Degradado", Role.VIEWER, true));

        mockMvc.perform(put("/api/v1/users/" + id)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    // ---------- deactivate ----------

    @Test
    @WithMockUser(roles = "ADMIN")
    void deactivate_conAdmin_returns204_ySoftDelete() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/users/" + id))
                .andExpect(status().isNoContent());

        // El controller delega al service; el service hace soft-delete (setActive(false)+save),
        // logica cubierta por el test unitario. Aqui basta con verificar la delegacion.
        verify(service).deactivate(id);
    }

    // ---------- reset password ----------

    @Test
    @WithMockUser(roles = "ADMIN")
    void resetPassword_conAdmin_returns204() throws Exception {
        UUID id = UUID.randomUUID();
        String body = objectMapper.writeValueAsString(new ChangePasswordRequest("NuevaPassword123"));

        mockMvc.perform(patch("/api/v1/users/" + id + "/password")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isNoContent());

        verify(service).resetPassword(eq(id), any());
    }
}
