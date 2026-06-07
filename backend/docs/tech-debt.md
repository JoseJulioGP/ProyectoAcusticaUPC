# Backend · Deuda técnica viva

Inventario de deudas técnicas reconocidas que NO se cerraron en el sprint actual. Se actualiza al cierre de cada sprint.

---

## Sprint 8 — estado actualizado

### D1 · Testcontainers bloqueado en Windows · sigue abierto, propuesta de cierre vía CI

**Test afectado:** `AnalyticsTrendControllerIntegrationTest` (`@Disabled` desde Sprint 7, sigue así en Sprint 8).

**Resumen:** ningún dev del equipo tiene Docker funcional para Testcontainers en su máquina local de Windows. Probado por Dev 1 (Sprint 7), Dev 2 (Sprint 7) y vuelto a probar en Sprint 8 (Dev 2). Mismo síntoma:

```
docker info → failed to connect to the docker API at
  npipe:////./pipe/dockerDesktopLinuxEngine: el sistema no puede encontrar
  el archivo especificado.
docker version --format "{{.Server.Version}}" → server=  (vacío)
```

| Sprint | Dev | Máquina | Intento | ¿Funciona? |
|---|---|---|---|---|
| 7 | Dev 1 (Mario) | Windows + Docker Desktop WSL 2 | Toggle "Allow default Docker socket" | ❌ |
| 7 | Dev 2 (JoseJulioGP) | Windows + Docker Desktop WSL 2 | `docker info` directo | ❌ |
| 8 | Dev 2 (JoseJulioGP) | Windows + Docker Desktop WSL 2 | Re-intento `docker info` tras reinicio | ❌ (mismo error) |

**Propuesta de cierre (Sprint 9, CI/CD):** mover la ejecución de los IT que extienden `AbstractPostgresIT` a **GitHub Actions** con el runner `ubuntu-latest`, que trae Docker nativo (sin Docker Desktop, sin WSL 2). Esquema sugerido:

```yaml
# .github/workflows/test-postgres-it.yml
name: Postgres-only IT
on:
  pull_request:
    branches: [develop, main]
jobs:
  postgres-it:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { distribution: temurin, java-version: 25 }
      - name: Run Postgres-only ITs
        working-directory: backend
        run: mvn -B test -DforkCount=0 -Dgroups=postgres-only
```

Pre-requisitos en el código antes de activar el workflow:
1. Etiquetar los ITs que extienden `AbstractPostgresIT` con `@Tag("postgres-only")` (JUnit 5).
2. Quitar `@Disabled` de `AnalyticsTrendControllerIntegrationTest`.
3. Configurar surefire en el `pom.xml` para que `mvn test` por defecto **excluya** `postgres-only` (los devs locales sin Docker siguen verdes; el workflow específico los incluye con `-Dgroups`).

**Hasta entonces:** el test se queda `@Disabled`. La suite local `mvn test` lo marca como `skipped`, no `failed`.

---

### D2 · `ApiError` sin campo `code` estructurado · ✅ CERRADO en Sprint 8

Cerrado en el commit 2 del Sprint 8. `ApiError` ahora tiene campo `code` nullable y los handlers de `IllegalStateException` (409) y `DomainException` (400) extraen el código del prefijo `^[A-Z][A-Z0-9_]*:` del `message`. Ver `backend/docs/sprint8-reports.md` para el detalle.

---

### D3 · ITs existentes usan H2, no Postgres real · sigue abierto

Sin cambios respecto al Sprint 7. La unificación a Testcontainers depende de D1 (Docker funcional en al menos un entorno de ejecución — local o CI). La propuesta de CI de D1 desbloquea esta deuda también: una vez que el workflow `postgres-it` corra verde, se pueden migrar progresivamente los ITs que usan SQL nativo Postgres-only a `AbstractPostgresIT`.

---
