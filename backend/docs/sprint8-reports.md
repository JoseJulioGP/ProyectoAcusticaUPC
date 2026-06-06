# Sprint 8 · Reportes — Excel y PDF del dashboard

Documentación de los dos endpoints de export del dashboard, más el diagnóstico
del bug del Excel que se nos reportó en producción.

---

## Endpoints

| Método | Path | Rol mínimo | Content-Type | Salida |
|---|---|---|---|---|
| GET | `/api/v1/reports/dashboard.xlsx` | autenticado (ADMIN, ANALYST, VIEWER) | `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet` | Workbook XLSX con 6 hojas |
| GET | `/api/v1/reports/dashboard.pdf` (Sprint 8, nuevo) | autenticado (ADMIN, ANALYST, VIEWER) | `application/pdf` | PDF A4 horizontal con 6 secciones |

Ambos exigen los query params:

- `from` (`YYYY-MM-DD`, requerido)
- `to` (`YYYY-MM-DD`, requerido)
- `zoneId` (UUID, opcional)

Sin `from` o `to` → 400 `Parámetro obligatorio ausente: …`. Sin token → 401.

Ambos cabeceras:

- `Content-Disposition: attachment; filename=dashboard.{xlsx|pdf}`
- `Content-Type` el del archivo respectivo.

CORS expone `Content-Disposition` (configurado en `SecurityConfig`).

### Contenido (idéntico entre Excel y PDF)

| Sección | Origen de datos |
|---|---|
| Resumen (KPIs) | `KpiService.computeKpis(from, to, zoneId, null)` |
| Por hora | `TimeSeriesService.series(... HOUR)` |
| Por día | `TimeSeriesService.series(... DAY)` |
| Por zona | `ZoneStatsService.statsForAllZones(from, to)` |
| No conformidades | `AlertService.findAlerts(zoneId, from, to, null, unpaged)` |
| Observaciones | `MeasurementBatchRepository.findByObservationIsNotNull()` |

Implementaciones:
- `DashboardExcelService` (Apache POI) — Sprint 7.
- `DashboardPdfService` (OpenPDF) — Sprint 8.

---

## Diagnóstico del bug del Excel

**Reporte original:** "El botón de Excel no funciona actualmente."

**Estado del backend (Sprint 8):** el endpoint y el servicio están **correctos**. Lo
verifiqué auditando todo el path:

1. **Endpoint** `GET /api/v1/reports/dashboard.xlsx`:
   - `@PreAuthorize("isAuthenticated()")` ← cualquier rol autenticado pasa.
   - `produces = application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`.
   - `from` y `to` requeridos (sin ellos, 400 antes del 200).
   - Devuelve `ResponseEntity.ok()` con header `Content-Disposition: attachment; filename=dashboard.xlsx` y `Content-Type` correcto.

2. **CORS** (`SecurityConfig`):
   - `setExposedHeaders(List.of("Content-Disposition"))` ya está presente.
   - Esto es **crítico**: si no estuviera expuesto, el navegador NO permite que el
     JS lea el header para descubrir el filename del blob.

3. **Test de integración** (`DashboardExcelControllerIntegrationTest`):
   - VIEWER recibe 200 + content-type + content-disposition correctos.
   - Sin token → 401.

**Causas probables (en frontend, fuera del backend):**

| Síntoma | Hipótesis |
|---|---|
| 400 al pulsar el botón | El frontend no está enviando `from`/`to`. Verificar que el formulario los rellena. |
| Descarga sin nombre / como `unknown` | El JS no lee `Content-Disposition`. Asegurar `fetch(...).then(r => { … r.headers.get('content-disposition') … })` y que el cliente está autenticado en el origen correcto (CORS). |
| 401 al hacer click | El JWT no se está adjuntando al fetch del binario. En `lib/api.js` el header `Authorization: Bearer …` debe ir también en el método `blob` (no solo en JSON). |
| El archivo se "descarga" pero queda vacío / no se abre en Excel | Algún proxy/CDN está envolviendo el body o cambiando el content-type. Revisar en DevTools la pestaña Network, columna **Type** = `xlsx`. |

**Acción recomendada para Dev 3:** abrir DevTools → Network → click en el
botón → mirar:
- Status code (200/400/401).
- Request headers (¿lleva `Authorization`?).
- Response headers (¿llega `Content-Type` correcto, `Content-Disposition` con
  filename?).

Si todos los tres están bien y el bug persiste, abrir un issue con el HAR del
request adjunto y lo revisamos juntos. **Mientras tanto, no toco código del
backend** porque la auditoría no encuentra defecto.

---

## ApiError.code (deuda D2 cerrada)

A partir de Sprint 8, el `ApiError` lleva un campo `code` (nullable) con el
código de negocio estable cuando el `message` empieza con un prefijo
`^[A-Z][A-Z0-9_]*:`.

Esto evita que el frontend tenga que hacer `message.split(':')[0]`.

| Endpoint | Excepción | message original | code |
|---|---|---|---|
| `DELETE /folders/{id}` (carpeta con batches) | `IllegalStateException` | `"FOLDER_IN_USE: la carpeta tiene batches..."` | `FOLDER_IN_USE` |
| `DELETE /folders/{id}` (carpeta con subcarpetas) | `IllegalStateException` | `"FOLDER_HAS_CHILDREN: …"` | `FOLDER_HAS_CHILDREN` |
| `DELETE /zones/{id}` (zona con batches) | `IllegalStateException` | `"ZONE_IN_USE: …"` | `ZONE_IN_USE` |
| `POST /auth/login` rate limit | `RateLimitFilter` directo | — | `RATE_LIMITED` (en el body crudo, no pasa por `ApiError`) |
| Cualquier `IllegalStateException` / `DomainException` sin prefijo | — | texto libre | `null` |

El `message` mantiene el prefijo por compatibilidad con clientes que aún no
leen `code`.

Ejemplo de respuesta:

```json
{
  "timestamp": "2026-06-04T15:23:11.000-05:00",
  "status": 409,
  "error": "Conflict",
  "code": "FOLDER_IN_USE",
  "message": "FOLDER_IN_USE: la carpeta tiene batches asociados; reasígnalos a otra carpeta primero",
  "path": "/api/v1/folders/9f3d…"
}
```

**Para Dev 3 (frontend):**
- Si `error.code === 'FOLDER_IN_USE'`, mostrar UI "esta carpeta tiene archivos,
  reasígnalos primero".
- Si `error.code` viene `null`, fallback al `error.message` como texto plano.
- No splitear `message` por `:`: usar `code`.
