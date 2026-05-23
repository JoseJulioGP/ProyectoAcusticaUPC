package com.upc.acusticupc.auth.application.dto;

import com.upc.acusticupc.auth.domain.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Nombre es obligatorio")
        @Size(max = 120)
        String fullName,

        @Email @NotBlank
        String email,

        @NotBlank
        @Size(min = 8, message = "Password mínimo 8 caracteres")
        String password,

        Role role
) {}