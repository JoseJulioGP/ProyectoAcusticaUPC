# Backend · Deuda técnica viva

Inventario de deudas técnicas reconocidas que NO se cerraron en el sprint actual. Se actualiza al cierre de cada sprint.

---

## Sprint 7 — abierto

### D1 · Testcontainers bloqueado en Windows con Docker Desktop (WSL 2 engine)

**Test afectado:** `AnalyticsTrendControllerIntegrationTest` (`@Disabled` desde Sprint 7 Dev 1).

**Síntoma:**
- `AnalyticsTrendControllerIntegrationTest` queries usan `EXTRACT(DOW FROM …)` que H2 no soporta. El IT se diseñó para correr contra Postgres real vía Testcontainers.
- Docker Desktop con engine WSL 2 expone `//./pipe/dockerDesktopLinuxEngine`, pero `docker-java` (cliente de Testcontainers) sondea por defecto `//./pipe/docker_engine`.
- Resultado: el contenedor no arranca, el test queda `@Disabled`.

**Estado en el repo:**

| Sprint | Dev | Máquina | ¿Funciona? |
|---|---|---|---|
| 7 | Dev 1 (Mario) | Windows + Docker Desktop WSL 2 | ❌ |
| 7 | Dev 2 (JoseJulioGP) | Windows + Docker Desktop WSL 2 | ❌ (`docker info` falla con `open //./pipe/dockerDesktopLinuxEngine: El sistema no puede encontrar el archivo especificado`) |

**Cómo intentar resolverlo (no probado verde aún):**

1. En Docker Desktop: **Settings → Advanced → "Allow the default Docker socket to be used (requires password)" → Apply & Restart**.
2. Alternativa: variable de entorno permanente en Windows:
   ```
   DOCKER_HOST=npipe:////./pipe/dockerDesktopLinuxEngine
   ```
3. Verificar con `docker info` desde una **terminal nueva** (no la sesión donde se cambió la variable).
4. Confirmar también con un smoke test rápido: `docker run --rm hello-world`.

**Cuando un dev consiga que pase verde en su máquina:**

- Quitar el `@Disabled` de `AnalyticsTrendControllerIntegrationTest`.
- Correr `mvn test -DforkCount=0` localmente y confirmar.
- Añadir las instrucciones que funcionaron al `README.md` del backend (sección "Tests" o "Setup").
- Cerrar este punto en `tech-debt.md`.

**Hasta entonces:** el test se queda en `@Disabled`. El CI no debe fallar por su ausencia — `mvn test` lo marca como `skipped`, no `failed`.

---

### D2 · `ApiError` sin campo `code` estructurado

**Estado:** abierto, pospuesto a Sprint 8 (opcional en Sprint 7).

**Problema:**
- Los códigos de error de dominio (`FOLDER_IN_USE`, `FOLDER_HAS_CHILDREN`, `ZONE_IN_USE`, `RATE_LIMITED`, etc.) viajan inline en el campo `message` del `ApiError`.
- El frontend, para reaccionar a un caso concreto, tiene que parsear el `message` por texto. Frágil cuando se cambia el copy.

**Propuesta:**
- Añadir un campo `code` (String, nullable) al record `ApiError`.
- Modificar los `@ExceptionHandler` para poblarlo cuando exista un código estable (por ahora: `RATE_LIMITED` ya lo tiene fuera del `ApiError` — el `RateLimitFilter` escribe `{"code":"RATE_LIMITED",…}` directo).
- DTOs típicos: `FOLDER_IN_USE` (409), `FOLDER_HAS_CHILDREN` (409), `ZONE_IN_USE` (409), `MITIGATION_CODE_DUPLICATE` (409).

**Por qué no se cierra en Sprint 7:**
- Backward-compatible (campo nullable, no rompe consumidores actuales).
- Pero altera la firma del record `ApiError` y, una vez añadido, hay que pasar por *todos* los handlers para decidir cuándo poblarlo. Encaja mejor con el Sprint 8 cuando el frontend lo necesite explícitamente.

---

### D3 · ITs existentes usan H2, no Postgres real

**Estado:** abierto, pospuesto a Sprint 8.

**Resumen:**
- Los 12+ ITs heredados se movieron a H2 en el blindaje del Sprint 7 (antes apuntaban a Supabase producción — bug crítico cerrado).
- Verdes en H2 con `ddl-auto: create-drop` + Flyway off.
- **Riesgo:** no validan SQL nativo Postgres-only. Ej.: el bug `log(integer, double precision)` del Sprint 4 no se atrapó en H2.

**Plan:**
- Unificar todos los ITs a Testcontainers + Postgres en Sprint 8. Depende de cerrar D1 primero (Docker funcional en al menos una máquina de dev/CI).

---
