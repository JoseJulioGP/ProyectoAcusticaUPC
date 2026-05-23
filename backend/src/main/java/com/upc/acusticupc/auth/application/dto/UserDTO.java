package com.upc.acusticupc.auth.application.dto;

import com.upc.acusticupc.auth.domain.model.Role;

import java.util.UUID;

public record UserDTO(
        UUID id,
        String fullName,
        String email,
        Role role,
        boolean active
) {}
