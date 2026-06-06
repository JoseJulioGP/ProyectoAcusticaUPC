package com.upc.acusticupc.auth.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Cambio de contraseña propia por un usuario autenticado.
 * Distinto de {@link ChangePasswordRequest} (reset de admin, solo newPassword):
 * aquí se exige la contraseña actual para validar al titular de la cuenta.
 */
public record ChangeOwnPasswordRequest(
        @NotBlank
        String currentPassword,

        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{10,100}$",
                message = "Mínimo 10 caracteres, una mayúscula, una minúscula y un dígito"
        )
        String newPassword
) {}
