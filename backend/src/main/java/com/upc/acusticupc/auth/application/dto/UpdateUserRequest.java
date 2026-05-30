package com.upc.acusticupc.auth.application.dto;

import com.upc.acusticupc.auth.domain.model.Role;
import jakarta.validation.constraints.*;

public record UpdateUserRequest(
        @NotBlank @Size(max = 120) String fullName,
        @NotNull Role role,
        @NotNull Boolean active
) {}
