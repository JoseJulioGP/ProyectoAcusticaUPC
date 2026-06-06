# Sprint 8 — Dev 1 (Mario) — Endpoints

Documentación de los cambios de backend ligero del Sprint 8.

---

## 1. Cambio de contraseña propia

`PATCH /api/v1/auth/change-password`

Un usuario **autenticado** cambia **su propia** contraseña (no requiere ser ADMIN).
Requiere la contraseña actual para verificar al titular de la cuenta.

| | |
|---|---|
| **Método / Path** | `PATCH /api/v1/auth/change-password` |
| **Autorización** | Autenticado (cualquier rol, sobre su propia cuenta) |
| **Body** | `ChangeOwnPasswordRequest` |
| **Respuesta OK** | `204 No Content` |

### Request body

```json
{
  "currentPassword": "MiPasswordActual1",
  "newPassword": "MiNuevoPassword1"
}
```

- `currentPassword`: obligatorio (`@NotBlank`).
- `newPassword`: debe cumplir el patrón **mínimo 10 caracteres, al menos una mayúscula,
  una minúscula y un dígito** (`^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{10,100}$`).

### Respuestas

| Código | Caso |
|---|---|
| `204 No Content` | Cambio exitoso. |
| `400 Bad Request` | Contraseña actual incorrecta. Mensaje contiene `CURRENT_PASSWORD_INVALID`. |
| `400 Bad Request` | `newPassword` no cumple el patrón de fortaleza (Bean Validation). |
| `401 Unauthorized` | Sin token / sesión inválida. |

> **Nota de deuda:** el código `CURRENT_PASSWORD_INVALID` viaja inline en el `message`
> del `ApiError` porque `ApiError` aún no tiene campo `code`. Dev 2 puede cerrar esa deuda.

> **Nota de diseño:** se creó un DTO nuevo `ChangeOwnPasswordRequest` (con `currentPassword`
> + `newPassword`) en vez de reutilizar el `ChangePasswordRequest` existente, que ya usa el
> reset de admin (`PATCH /api/v1/users/{id}/password`, solo `newPassword`). Así no se rompe
> el flujo de administración.

---

## 2. Rol ANALYST en analytics y compliance (bug cerrado)

Antes, estos endpoints tenían `@PreAuthorize("hasAnyRole('ADMIN','VIEWER')")` y **excluían a
ANALYST**, que es justo el rol que más necesita analítica y cumplimiento. Esto generaba `403`
que el frontend interpretaba como sesión inválida.

Ahora aceptan `ADMIN`, `ANALYST` y `VIEWER`.

### Analytics — `AnalyticsController` (anotación a nivel de clase)

`hasAnyRole('ADMIN','ANALYST','VIEWER')` aplica a todos sus endpoints:

| Método | Path |
|---|---|
| GET | `/api/v1/analytics/kpis` |
| GET | `/api/v1/analytics/zones/stats` |
| GET | `/api/v1/analytics/zones/{zoneId}` |
| GET | `/api/v1/analytics/alerts/summary` |
| GET | `/api/v1/analytics/timeseries` |
| GET | `/api/v1/analytics/heatmap` |
| GET | `/api/v1/analytics/comparison` |
| GET | `/api/v1/analytics/weekday` |
| GET | `/api/v1/analytics/before-after` |

### Compliance — `ComplianceController` (por endpoint de lectura)

| Método | Path | Rol |
|---|---|---|
| GET | `/api/v1/compliance/results/batch/{batchId}` | + ANALYST |
| GET | `/api/v1/compliance/results` | + ANALYST |
| GET | `/api/v1/compliance/alerts` | + ANALYST |

> `POST /api/v1/compliance/evaluate/{batchId}` **sigue siendo solo ADMIN** (es un disparo de
> escritura/evaluación, no lectura).

---

## Para Dev 3 (frontend) — consumir change-password desde el modal de perfil

Flujo sugerido para el modal "Cambiar contraseña":

1. Form con tres campos: `currentPassword`, `newPassword`, `confirmNewPassword`
   (la confirmación se valida solo en el cliente).
2. Validar en cliente el patrón de `newPassword` antes de enviar
   (mínimo 10, una mayúscula, una minúscula, un dígito) para feedback inmediato.
3. Petición:

```ts
await api.patch('/api/v1/auth/change-password', {
  currentPassword,
  newPassword,
});
// 204 → cerrar modal, toast "Contraseña actualizada"
```

4. Manejo de errores:
   - `400` con mensaje que contiene `CURRENT_PASSWORD_INVALID` → marcar el campo
     "contraseña actual" como incorrecto (no cerrar sesión).
   - `400` por validación de patrón → mostrar el `message` del `ApiError` bajo el campo
     "nueva contraseña".
   - `401` → sí es sesión inválida → redirigir a login.

> Importante: distinguir el `400` de contraseña actual incorrecta de un `401` real. El `401`
> es el único que debe forzar logout; el `400` se queda dentro del modal.
