package com.upc.acusticupc.shared.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.upc.acusticupc.auth.application.service.UserManagementService.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Sprint 8 — extrae un código de negocio del prefijo de un mensaje con la forma
     * {@code "FOO_BAR: descripción legible"}. Si no calza, devuelve {@code null}.
     */
    private static final Pattern CODE_PREFIX = Pattern.compile("^([A-Z][A-Z0-9_]*):");

    private static String extractCode(String message) {
        if (message == null) return null;
        Matcher m = CODE_PREFIX.matcher(message);
        return m.find() ? m.group(1) : null;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex, HttpServletRequest req) {
        log.warn("Recurso no encontrado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ApiError.of(404, "Not Found", ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiError> handleDomain(DomainException ex, HttpServletRequest req) {
        log.warn("Violación de regla de dominio: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ApiError.of(400, "Domain Error", extractCode(ex.getMessage()),
                    ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
            .map(f -> f.getField() + ": " + f.getDefaultMessage())
            .collect(Collectors.joining("; "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ApiError.of(400, "Validation Error", msg, req.getRequestURI()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsException ex, HttpServletRequest req) {
        log.warn("Credenciales inválidas en {}", req.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            ApiError.of(401, "Unauthorized", "Credenciales inválidas", req.getRequestURI()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        log.warn("Acceso denegado: {} en {}", ex.getMessage(), req.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
            ApiError.of(403, "Forbidden", "No tienes permisos para acceder a este recurso", req.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest req) {
        log.error("Error no manejado", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            ApiError.of(500, "Internal Server Error",
                "Ocurrió un error inesperado", req.getRequestURI()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> badArgument(IllegalArgumentException ex, HttpServletRequest req) {
        log.warn("Argumento inválido: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiError.of(400, "Bad Request", ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> illegalState(IllegalStateException ex, HttpServletRequest req) {
        log.warn("Estado inválido para la operación: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ApiError.of(409, "Conflict", extractCode(ex.getMessage()),
                        ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> missingParam(MissingServletRequestParameterException ex,
                                                 HttpServletRequest req) {
        String msg = "Parámetro obligatorio ausente: " + ex.getParameterName();
        log.warn("{} en {}", msg, req.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiError.of(400, "Bad Request", msg, req.getRequestURI()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiError> userNotFound(UserNotFoundException ex, HttpServletRequest req) {
        log.warn("Usuario no encontrado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ApiError.of(404, "Not Found", ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(EmailAlreadyUsedException.class)
    public ResponseEntity<ApiError> emailUsed(EmailAlreadyUsedException ex, HttpServletRequest req) {
        log.warn("Email duplicado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ApiError.of(409, "Conflict", ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(LastAdminException.class)
    public ResponseEntity<ApiError> lastAdmin(LastAdminException ex, HttpServletRequest req) {
        log.warn("Intento de tocar al último admin: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ApiError.of(409, "Conflict", ex.getMessage(), req.getRequestURI()));
    }

}