package com.upc.acusticupc.auth.application.dto;

public record AuthResponse(
        String token,
        String tokenType,
        long expiresInMs,
        UserDTO user
) {
    public static AuthResponse of(String token, long expiresInMs, UserDTO user) {
        return new AuthResponse(token, "Bearer", expiresInMs, user);
    }
}