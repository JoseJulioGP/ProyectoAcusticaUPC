package com.upc.acusticupc.auth.infrastructure.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Sprint 7 — Bloque B · Rate limiting in-memory en endpoints públicos de auth.
 *
 * <ul>
 *   <li>{@code POST /api/v1/auth/login}: 10 intentos / 10 minutos por IP.</li>
 *   <li>{@code POST /api/v1/auth/register}: 5 intentos / hora por IP.</li>
 * </ul>
 *
 * <p>Cuando se excede, responde {@code 429 Too Many Requests} con
 * {@code {"code":"RATE_LIMITED","message":"Demasiados intentos"}}.</p>
 *
 * <p>Buckets almacenados en {@link ConcurrentMap} por IP. Es suficiente para una
 * instancia única; si en producción se escala a varias instancias detrás de
 * un balanceador, hace falta mover el almacenamiento a Redis (ver
 * bucket4j-redis). Registrado en {@link SecurityConfig} con
 * {@code addFilterBefore(rateLimitFilter, JwtAuthenticationFilter.class)}.</p>
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    static final String LOGIN_PATH = "/api/v1/auth/login";
    static final String REGISTER_PATH = "/api/v1/auth/register";

    private static final String RATE_LIMITED_BODY =
            "{\"code\":\"RATE_LIMITED\",\"message\":\"Demasiados intentos\"}";

    private final ConcurrentMap<String, Bucket> loginBuckets = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Bucket> registerBuckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        // Solo POST sobre los dos endpoints públicos; el resto pasa sin tocar.
        if ("POST".equalsIgnoreCase(request.getMethod())) {
            String uri = request.getRequestURI();
            Bucket bucket = null;
            if (LOGIN_PATH.equals(uri)) {
                bucket = loginBuckets.computeIfAbsent(clientKey(request), k -> newLoginBucket());
            } else if (REGISTER_PATH.equals(uri)) {
                bucket = registerBuckets.computeIfAbsent(clientKey(request), k -> newRegisterBucket());
            }
            if (bucket != null && !bucket.tryConsume(1)) {
                writeRateLimited(response);
                return;
            }
        }
        chain.doFilter(request, response);
    }

    /** Clave por IP del cliente. Si en el futuro hay proxy, usar X-Forwarded-For. */
    private String clientKey(HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        return (ip == null || ip.isBlank()) ? "unknown" : ip;
    }

    private void writeRateLimited(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(RATE_LIMITED_BODY);
    }

    private static Bucket newLoginBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(10)
                .refillIntervally(10, Duration.ofMinutes(10))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }

    private static Bucket newRegisterBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(5)
                .refillIntervally(5, Duration.ofHours(1))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }
}
