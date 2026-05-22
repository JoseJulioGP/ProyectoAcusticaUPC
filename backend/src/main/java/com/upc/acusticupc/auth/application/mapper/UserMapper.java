package com.upc.acusticupc.auth.application.mapper;

import com.upc.acusticupc.auth.application.dto.UserDTO;
import com.upc.acusticupc.auth.domain.model.User;

public final class UserMapper {

    private UserMapper() {}

    public static UserDTO toDto(User user) {
        return new UserDTO(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.isActive()
        );
    }
}
