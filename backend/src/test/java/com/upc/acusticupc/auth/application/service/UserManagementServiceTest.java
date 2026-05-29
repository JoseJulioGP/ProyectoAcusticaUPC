package com.upc.acusticupc.auth.application.service;

import com.upc.acusticupc.auth.application.dto.CreateUserRequest;
import com.upc.acusticupc.auth.application.service.UserManagementService.EmailAlreadyUsedException;
import com.upc.acusticupc.auth.application.service.UserManagementService.LastAdminException;
import com.upc.acusticupc.auth.domain.model.Role;
import com.upc.acusticupc.auth.domain.model.User;
import com.upc.acusticupc.auth.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserManagementServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks UserManagementService service;

    @Test
    void create_emailDuplicado_lanzaEmailAlreadyUsedException() {
        when(userRepository.existsByEmailIgnoreCase("nuevo@upc.edu.co")).thenReturn(true);

        CreateUserRequest req = new CreateUserRequest(
                "Nuevo Usuario", "Nuevo@upc.edu.co", Role.ANALYST, "Password123");

        assertThatThrownBy(() -> service.create(req))
                .isInstanceOf(EmailAlreadyUsedException.class)
                .hasMessageContaining("nuevo@upc.edu.co");

        verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void deactivate_ultimoAdminActivo_lanzaLastAdminException() {
        UUID id = UUID.randomUUID();
        User admin = User.builder()
                .id(id)
                .fullName("Admin")
                .email("admin@upc.edu.co")
                .passwordHash("hashed")
                .role(Role.ADMIN)
                .active(true)
                .build();
        when(userRepository.findById(id)).thenReturn(Optional.of(admin));
        when(userRepository.countByRoleAndActiveTrue(Role.ADMIN)).thenReturn(1L);

        assertThatThrownBy(() -> service.deactivate(id))
                .isInstanceOf(LastAdminException.class);

        verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
