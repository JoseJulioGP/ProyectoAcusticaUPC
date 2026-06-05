# Security notes · AcústicaUPC backend

Registro de vulnerabilidades, hardening y decisiones de seguridad. Se actualiza por sprint.

---

## Sprint 7

### SEC-S7-01 · Privilege escalation en `POST /auth/register`

**Estado:** ✅ cerrado.
**Severidad:** crítica (escalada de privilegios desde un endpoint público).
**Fecha de cierre:** Sprint 7, primer commit del Bloque A.

**Resumen**

`POST /api/v1/auth/register` es un endpoint público (`permitAll()` en `SecurityConfig`). Antes del Sprint 7, el body aceptaba un campo opcional `role` que se traspasaba sin filtros al servicio:

```java
// AuthServiceImpl.register() — código vulnerable
.role(request.role() != null ? request.role() : Role.VIEWER)
```

Cualquier anónimo podía hacer:

```bash
curl -X POST .../api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"x@y.z","password":"…","fullName":"x","role":"ADMIN"}'
```

y quedar con rol **ADMIN** sin pasar por la gestión de usuarios (`POST /api/v1/users`, restringida a ADMIN).

**Cómo se cerró**

Doble blindaje (defensa en profundidad):

1. **Eliminé el campo `role` del DTO** `RegisterRequest`. Jackson ahora ignora silenciosamente cualquier `"role": "ADMIN"` que llegue en el body como propiedad desconocida. El servicio **no tiene de dónde leerlo**.
2. **Hardcoded `Role.VIEWER` en el servicio**. La línea quedó:
   ```java
   // AuthServiceImpl.register() — código corregido
   .role(Role.VIEWER)
   ```
   Sin ternaria, sin fallback al body. El alta de ADMIN/ANALYST sigue siendo responsabilidad de `UserController` (`POST /api/v1/users`, `@PreAuthorize("hasRole('ADMIN')")`).

**Tests que validan el cierre**

- `AuthServiceImplTest.register_alwaysCreatesUserWithRoleViewer`
  Unit con Mockito: llama al servicio desde Java y verifica con `ArgumentCaptor<User>` que la entidad persistida tiene `role = VIEWER` y `active = true`.
- `AuthRegisterPrivilegeEscalationIntegrationTest.registerConRoleAdminEnBody_usuarioQuedaComoVIEWER`
  IT end-to-end: POST `/auth/register` con `"role":"ADMIN"` en JSON crudo → POST `/auth/login` para obtener token → GET `/auth/me`. Asserta `role == "VIEWER"` en cada paso (respuesta del register, JWT del login y `/me`). Cubre el camino HTTP real que era la vulnerabilidad.

Ambos tests viven en la suite obligatoria de CI: cualquier regresión a la ternaria o re-introducción del campo `role` en el DTO los rompe inmediatamente.

---

### SEC-S7-02 · Hardening de auth — endpoints públicos

**Estado:** ✅ entregado en Sprint 7, Bloque B.

**Medidas:**

1. **Rate limiting in-memory** (`RateLimitFilter`, bucket4j 8.10.1) sobre los endpoints públicos:
   - `POST /api/v1/auth/login`: 10 intentos / 10 minutos por IP.
   - `POST /api/v1/auth/register`: 5 intentos / hora por IP.
   - Excedido → `429 Too Many Requests` con `{"code":"RATE_LIMITED","message":"Demasiados intentos"}`.
   - Limitación: in-memory por instancia. Si el backend escala a múltiples réplicas detrás de un LB, mover storage a Redis (`bucket4j-redis`).

2. **Política de contraseña fuerte** en `RegisterRequest`:
   - Regex: `^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{10,100}$`
   - Mínimo 10 caracteres, una mayúscula, una minúscula y un dígito.
   - Mensaje devuelto al cliente al fallar: `"Mínimo 10 caracteres, una mayúscula, una minúscula y un dígito"`.

3. **Matriz rol × endpoint del Sprint 7** (`RoleEndpointAccessMatrixIntegrationTest`): 56 combinaciones que confirman 401 anónimo / 403 rol insuficiente / autz OK para los 14 endpoints relevantes (`/zones`, `/folders`, PATCH de `/ingest/batches`, `/alerts`, `/analytics/{weekday,before-after}`, `/reports/dashboard.xlsx`).

---

## Apéndice · Convenciones del proyecto sobre errores de auth

| Situación | HTTP | Manejado por |
|---|---|---|
| Sin token | `401` | `AuthenticationEntryPoint` configurado en `SecurityConfig` |
| Credenciales inválidas en `/auth/login` | `401` | `BadCredentialsException` → `GlobalExceptionHandler` |
| Token válido, rol insuficiente | `403` | `AccessDeniedHandler` (URL-level) o `GlobalExceptionHandler` (`@PreAuthorize`) |
| Validación de body (`@Valid`) | `400` | `MethodArgumentNotValidException` → `GlobalExceptionHandler` |
| Recurso inexistente | `404` | `ResourceNotFoundException(resource, id)` → `GlobalExceptionHandler` |
| Conflicto de unicidad (email/code duplicado) | `409` | `EmailAlreadyUsedException` / `IllegalStateException` → `GlobalExceptionHandler` |
| Estado inválido para la operación (p. ej. retry sobre batch COMPLETED) | `409` | `IllegalStateException` → `GlobalExceptionHandler` |
| Rate limit excedido | `429` | `RateLimitFilter` (cuerpo `{"code":"RATE_LIMITED",…}`) |
