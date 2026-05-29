package com.upc.acusticupc.auth.application.dto;

import com.upc.acusticupc.auth.domain.model.Role;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserListItemDTO(
        UUID id,
        String fullName,
        String email,
        Role role,
        boolean active,
        OffsetDateTime createdAt
) {}
