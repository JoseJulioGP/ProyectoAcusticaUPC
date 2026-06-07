package com.upc.acusticupc.shared.exception;

import java.time.OffsetDateTime;

/**
 * Cuerpo JSON estándar de error del proyecto.
 *
 * <p>Sprint 8 — cierre de deuda D2: campo {@code code} (nullable) para que el
 * frontend pueda reaccionar a códigos de error estables sin parsear el
 * {@code message}. Ej.: {@code FOLDER_IN_USE}, {@code ZONE_IN_USE},
 * {@code FOLDER_HAS_CHILDREN}, {@code RATE_LIMITED}.</p>
 */
public record ApiError(
    OffsetDateTime timestamp,
    int status,
    String error,
    String code,
    String message,
    String path
) {
    /** Overload retrocompatible para handlers que no exponen código estructurado. */
    public static ApiError of(int status, String error, String message, String path) {
        return new ApiError(OffsetDateTime.now(), status, error, null, message, path);
    }

    /** Variante con código de negocio estable para el frontend. */
    public static ApiError of(int status, String error, String code, String message, String path) {
        return new ApiError(OffsetDateTime.now(), status, error, code, message, path);
    }
}
