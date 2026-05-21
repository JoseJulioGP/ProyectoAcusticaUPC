package com.upc.acusticupc.shared.exception;

import java.time.OffsetDateTime;

public record ApiError(
    OffsetDateTime timestamp,
    int status,
    String error,
    String message,
    String path
) {
    public static ApiError of(int status, String error, String message, String path) {
        return new ApiError(OffsetDateTime.now(), status, error, message, path);
    }
}