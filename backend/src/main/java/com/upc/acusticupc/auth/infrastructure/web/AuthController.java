package com.upc.acusticupc.auth.infrastructure.web;

import com.upc.acusticupc.auth.application.dto.AuthResponse;
import com.upc.acusticupc.auth.application.dto.ChangeOwnPasswordRequest;
import com.upc.acusticupc.auth.application.dto.LoginRequest;
import com.upc.acusticupc.auth.application.dto.RegisterRequest;
import com.upc.acusticupc.auth.application.dto.UserDTO;
import com.upc.acusticupc.auth.application.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> me(Authentication authentication) {
        return ResponseEntity.ok(authService.getCurrentUser(authentication.getName()));
    }

    /**
     * Un usuario autenticado cambia SU PROPIA contraseña. Requiere la contraseña actual.
     */
    @PatchMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(Authentication authentication,
                               @Valid @RequestBody ChangeOwnPasswordRequest request) {
        authService.changePassword(
                authentication.getName(), request.currentPassword(), request.newPassword());
    }
}
