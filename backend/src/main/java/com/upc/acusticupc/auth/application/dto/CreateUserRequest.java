package com.upc.acusticupc.auth.application.dto;

import com.upc.acusticupc.auth.domain.model.Role;
import jakarta.validation.constraints.*;

public record CreateUserRequest(
        @NotBlank @Size(max = 120) String fullName,
        @NotBlank @Email @Size(max = 160) String email,
        @NotNull Role role,
        @NotBlank @Size(min = 8, max = 72) String password
) {}
