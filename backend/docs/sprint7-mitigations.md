# API · `/api/v1/mitigations`

Catálogo de **acciones de mitigación** de ruido (Sprint 7 — RF#11). Permite listar el catálogo, sugerir acciones según el exceso de dB sobre el estándar y gestionar el catálogo (alta, edición, baja lógica).

- Base path: `/api/v1/mitigations`
- Autenticación: **JWT en `Authorization: Bearer <token>`** (todas las rutas).
- Estilo de errores: `ApiError` JSON estándar del proyecto (`timestamp`, `status`, `error`, `message`, `path`).
- Borrado: **soft-delete** (`active = false`). La fila se preserva en BD; el GET filtra por `active = true`.

## Resumen de endpoints

| Método | Path | Rol mínimo | Body | Status feliz |
|---|---|---|---|---|
| GET | `/api/v1/mitigations` | autenticado (ADMIN, ANALYST, VIEWER) | — | 200 |
| GET | `/api/v1/mitigations/suggest?excessDb={n}` | autenticado | — | 200 |
| POST | `/api/v1/mitigations` | ADMIN o ANALYST | `MitigationActionRequest` | 201 |
| PUT | `/api/v1/mitigations/{id}` | ADMIN o ANALYST | `MitigationActionRequest` | 200 |
| DELETE | `/api/v1/mitigations/{id}` | ADMIN | — | 204 |

---

## Modelo

### `MitigationActionRequest` (POST/PUT body)

```json
{
  "code": "M01",
  "title": "Instalar pantalla acústica",
  "description": "Barrera física entre fuente y receptor.",
  "regulationRef": "Res. 0627/2006 Art. 26",
  "priority": 1,
  "estimatedImpactDb": 8.0,
  "active": true
}
```

| Campo | Tipo | Obligatorio | Validación | Notas |
|---|---|---|---|---|
| `code` | string | sí | `@NotBlank`, max 16 | Único en el catálogo. En `PUT`, si cambia, se valida unicidad contra otras filas. |
| `title` | string | sí | `@NotBlank`, max 160 | |
| `description` | string | sí | `@NotBlank` | Texto largo. |
| `regulationRef` | string | no | max 120 | Referencia normativa (artículo/anexo Res. 0627). |
| `priority` | int | sí | 1 ≤ x ≤ 5 | 1 = alta, 5 = baja. |
| `estimatedImpactDb` | double | no | — | Reducción esperada en dB(A). |
| `active` | boolean | no | — | En `POST` defaultea a `true`; en `PUT` solo se toca si llega no-null. |

### `MitigationActionResponse` (cuerpo de respuesta)

```json
{
  "id": "9f3d…",
  "code": "M01",
  "title": "Instalar pantalla acústica",
  "description": "Barrera física entre fuente y receptor.",
  "regulationRef": "Res. 0627/2006 Art. 26",
  "priority": 1,
  "estimatedImpactDb": 8.0,
  "active": true,
  "createdAt": "2026-06-04T10:15:30-05:00",
  "updatedAt": null
}
```

---

## Endpoints detallados

### `GET /api/v1/mitigations`

Lista las acciones **activas**, ordenadas por `priority` ascendente (1 alta → 5 baja).

```bash
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/v1/mitigations
```

Códigos:
- `200 OK` — array (puede estar vacío).
- `401` — sin token o token inválido.

### `GET /api/v1/mitigations/suggest?excessDb={n}`

Sugiere acciones según el exceso de dB sobre el límite normativo:

| `excessDb` | Acciones devueltas |
|---|---|
| ≤ 5 | top **3** (acciones de bajo costo / fácil implementación) |
| (5, 10] | top **5** |
| > 10 | **todas** las activas (caso crítico) |

Orden: por `priority` ascendente. Si el catálogo tiene menos elementos que el "top N", se devuelven los que haya.

```bash
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/v1/mitigations/suggest?excessDb=8"
```

Códigos:
- `200 OK`
- `400` — `excessDb` ausente o no parseable como número.
- `401` — sin token.

### `POST /api/v1/mitigations`

Crea una acción. Roles: **ADMIN** o **ANALYST**.

```bash
curl -X POST -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"code":"M09","title":"Sello acústico de puertas","description":"…","priority":4}' \
  http://localhost:8080/api/v1/mitigations
```

Códigos:
- `201 Created` + body con la acción creada (incluye `id`, `createdAt`).
- `400` — body inválido (`@Valid`).
- `401` — sin token.
- `403` — rol insuficiente (VIEWER).
- `409 Conflict` — `code` ya existe.

### `PUT /api/v1/mitigations/{id}`

Actualiza una acción. Roles: **ADMIN** o **ANALYST**.

```bash
curl -X PUT -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"code":"M09","title":"Sello acústico (revisión 2)","description":"…","priority":3}' \
  http://localhost:8080/api/v1/mitigations/9f3d…
```

Códigos:
- `200 OK` + body actualizado (incluye `updatedAt`).
- `400` — body inválido.
- `401` — sin token.
- `403` — rol insuficiente.
- `404` — `id` inexistente.
- `409` — el nuevo `code` ya está usado por otra acción.

### `DELETE /api/v1/mitigations/{id}`

Desactiva la acción (soft-delete). Rol: **ADMIN** únicamente.

```bash
curl -X DELETE -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/v1/mitigations/9f3d…
```

Códigos:
- `204 No Content`.
- `401` — sin token.
- `403` — VIEWER o ANALYST.
- `404` — `id` inexistente.

La fila persiste en BD con `active = false` y `updatedAt` actualizado. No vuelve a aparecer en `GET /api/v1/mitigations`.

---

## Para Dev 3 (frontend)

Notas para el consumo desde el panel de alertas.

**1. Sugerencias contextuales por alerta**

Cuando se muestre una alerta, calcula `excessDb = measuredDb - standardDb` y llama:

```http
GET /api/v1/mitigations/suggest?excessDb=8
Authorization: Bearer <jwt>
```

Renderiza el array devuelto como tarjetas/listado. El orden ya viene por relevancia (`priority` asc) — no re-ordenar en el front.

**2. Catálogo completo (vista admin)**

Para una página de administración del catálogo (solo ADMIN/ANALYST tendrán botones de alta/edición; VIEWER lo ve en sólo-lectura):

```http
GET /api/v1/mitigations
```

Esconde **Crear / Editar / Eliminar** según el rol del usuario logueado (`auth.user.role`). El backend ya devuelve 403 si se intenta sin permiso — el front lo usa para UX, el backend para seguridad real.

**3. Severidad → color**

Sugerencia de paleta consistente con el resto de la app:

| `priority` | Etiqueta | Color guía |
|---|---|---|
| 1 | Alta | naranja (`rombo.naranja`) |
| 2 | Alta-media | naranja claro |
| 3 | Media | amarillo (`warn`) |
| 4–5 | Baja | gris (`muted`) |

**4. Errores 409**

Cuando el backend responda **409** al crear/editar, el `ApiError.message` ya trae texto humano (`"Ya existe una acción de mitigación con código M09"`). Mostrarlo tal cual al usuario en un toast/alert es suficiente; no es necesario parsearlo.

**5. Soft-delete y reactivación**

`DELETE` no borra: marca `active = false`. Si en el futuro se quiere reactivar, el `PUT` con `"active": true` lo permite (mismo endpoint, mismo permiso).

---

## Notas para QA

- Las semillas (M01..M08) provienen de la migración Flyway `V9__sprint7_mitigation_actions.sql`. En producción/staging las verás disponibles desde el primer arranque tras el merge.
- En entornos locales con `spring.flyway.enabled: false` (regla del Sprint 7 mientras prod no esté blindada), tendrás que insertarlas a mano o ejecutar la V9 contra una BD aislada.
- Los tests automatizados (`MitigationControllerIntegrationTest`) usan H2 + `ddl-auto: create-drop` y siembran 8 acciones T01..T08 en `@BeforeEach`. No leen la V9.
