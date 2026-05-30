package com.upc.acusticupc.auth.application.dto;

import jakarta.validation.constraints.*;

public record ChangePasswordRequest(
        @NotBlank @Size(min = 8, max = 72) String newPassword
) {}
