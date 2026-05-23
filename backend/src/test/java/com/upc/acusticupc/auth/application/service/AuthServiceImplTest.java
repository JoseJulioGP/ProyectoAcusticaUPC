package com.upc.acusticupc.auth.application.service;

import com.upc.acusticupc.auth.application.dto.AuthResponse;
import com.upc.acusticupc.auth.application.dto.LoginRequest;
import com.upc.acusticupc.auth.domain.model.Role;
import com.upc.acusticupc.auth.domain.model.User;
import com.upc.acusticupc.auth.domain.repository.UserRepository;
import com.upc.acusticupc.auth.infrastructure.security.JwtService;
import com.upc.acusticupc.shared.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;
    @Mock AuthenticationManager authenticationManager;

    @InjectMocks AuthServiceImpl service;

    private User admin;

    @BeforeEach
    void setup() {
        admin = User.builder()
                .id(UUID.randomUUID())
                .fullName("Admin")
                .email("admin@upc.edu.co")
                .passwordHash("hashed")
                .role(Role.ADMIN)
                .active(true)
                .build();
    }

    @Test
    void shouldLoginSuccessfullyAndReturnToken() {
        when(userRepository.findByEmailIgnoreCase("admin@upc.edu.co")).thenReturn(Optional.of(admin));
        when(jwtService.generateToken(anyString(), any())).thenReturn("jwt-token");
        when(jwtService.getExpirationMs()).thenReturn(3600000L);

        AuthResponse resp = service.login(new LoginRequest("admin@upc.edu.co", "AdminUpc2026!"));

        assertEquals("jwt-token", resp.token());
        assertEquals("Bearer", resp.tokenType());
        assertEquals("admin@upc.edu.co", resp.user().email());
    }

    @Test
    void shouldThrowWhenCredentialsAreInvalid() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        DomainException ex = assertThrows(DomainException.class,
                () -> service.login(new LoginRequest("admin@upc.edu.co", "wrong")));
        assertEquals("Credenciales inválidas", ex.getMessage());
    }

    @Test
    void shouldThrowWhenUserIsInactive() {
        admin.setActive(false);
        when(userRepository.findByEmailIgnoreCase("admin@upc.edu.co")).thenReturn(Optional.of(admin));

        DomainException ex = assertThrows(DomainException.class,
                () -> service.login(new LoginRequest("admin@upc.edu.co", "x")));
        assertEquals("La cuenta está inactiva", ex.getMessage());
    }
}
