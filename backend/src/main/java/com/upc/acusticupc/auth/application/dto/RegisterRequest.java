package com.upc.acusticupc.auth.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Payload del endpoint público {@code POST /api/v1/auth/register}.
 *
 * <p>Importante (Sprint 7, cierre de escalada de privilegios): el campo {@code role}
 * fue eliminado del DTO a propósito. El rol del usuario que se autoregistra siempre
 * es {@code VIEWER} (hardcoded en {@code AuthServiceImpl.register}). Cualquier
 * {@code "role": "ADMIN"} que llegue en el body es ignorado silenciosamente por
 * Jackson como propiedad desconocida.</p>
 */
public record RegisterRequest(
        @NotBlank(message = "Nombre es obligatorio")
        @Size(max = 120)
        String fullName,

        @Email @NotBlank
        String email,

        @NotBlank
        @Size(min = 8, message = "Password mínimo 8 caracteres")
        String password
) {}
