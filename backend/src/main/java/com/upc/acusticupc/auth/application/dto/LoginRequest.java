package com.upc.acusticupc.auth.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Email(message = "Email inválido")
        @NotBlank(message = "Email es obligatorio")
        String email,

        @NotBlank(message = "Password es obligatorio")
        String password
) {}
