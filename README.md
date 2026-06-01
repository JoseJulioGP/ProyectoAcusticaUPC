<div align="center">

# 🔊 AcústicaUPC

### Evaluación de fuentes generadoras de contaminación sonora en la Universidad Popular del Cesar

Sistema BI de monitoreo de ruido y verificación de cumplimiento contra la **Resolución 0627 de 2006 (MAVDT)**.

**Universidad Popular del Cesar** · Facultad de Ingeniería · Ingeniería Ambiental y Sanitaria · Valledupar · 2026

</div>

---

## 📑 Tabla de contenido

1. [Descripción](#-descripción)
2. [Stack tecnológico](#-stack-tecnológico)
3. [Arquitectura](#-arquitectura)
4. [Estructura del repositorio](#-estructura-del-repositorio)
5. [Modelo de dominio](#-modelo-de-dominio)
6. [Estándares Res. 0627 de 2006](#-estándares-resolución-0627-de-2006)
7. [Roles y permisos](#-roles-y-permisos)
8. [Puesta en marcha](#-puesta-en-marcha)
9. [Variables de entorno](#-variables-de-entorno)
10. [API REST](#-api-rest)
11. [Metodología y flujo de trabajo](#-metodología-y-flujo-de-trabajo)
12. [Historial de sprints](#-historial-de-sprints)
13. [Equipo](#-equipo)

---

## 📌 Descripción

AcústicaUPC ingiere mediciones de sonómetro (archivos `.xls`/`.xlsx`), las normaliza por zona y periodo (diurno/nocturno según la Resolución 0627), evalúa el cumplimiento contra los estándares máximos permisibles de **ruido ambiental (Tabla 2)** y presenta el resultado en un dashboard con KPIs, gráficas, mapa de calor y alertas. Permite exportar un reporte de cumplimiento en PDF.

Capacidades principales:
- **Ingesta** de archivos de sonómetro con procesamiento asíncrono e historial de cargas.
- **Dashboard** con LAeq por hora, comparativa diurno/nocturno por zona, mapa de calor y % de cumplimiento.
- **Cumplimiento**: evaluación por zona/periodo + alertas cuando se supera el límite.
- **Reporte PDF** del cumplimiento.
- **Gestión de usuarios** con roles (ADMIN / ANALYST / VIEWER).

---

## 🧰 Stack tecnológico

### Backend
- **Java 25**, **Spring Boot 4.0.6**
- Spring Web · Spring Security (JWT) · Spring Data JPA
- **PostgreSQL** (Supabase)
- **Apache POI** — lectura de `.xls`/`.xlsx`
- **OpenPDF** — generación del reporte PDF
- **Flyway** — migraciones de base de datos
- Procesamiento asíncrono con `@Async` (virtual threads)
- Build: **Maven**

### Frontend
- **React 18** + **Vite**
- **Tailwind CSS 3**
- React Router
- Fuentes: **Sora** (display) + **Manrope** (cuerpo)

### Infraestructura
- **Render** — hosting del backend
- **Vercel** — hosting del frontend
- **Supabase** — base de datos PostgreSQL gestionada

---

## 🏛 Arquitectura

```
┌──────────────┐      HTTPS / JWT      ┌────────────────────┐      JDBC       ┌──────────────┐
│   Frontend   │ ───────────────────▶ │      Backend       │ ──────────────▶ │  PostgreSQL  │
│ React + Vite │ ◀─────────────────── │   Spring Boot API  │ ◀────────────── │  (Supabase)  │
│   (Vercel)   │      JSON / PDF       │     (Render)       │                 └──────────────┘
└──────────────┘                       └────────────────────┘
                                          │  Ingesta @Async (POI)
                                          │  Evaluación Res. 0627
                                          ▼  Reporte PDF (OpenPDF)
```

Flujo de ingesta: subir archivo → batch `PENDING` → `@Async` parsea con POI → mediciones persistidas → evaluación de cumplimiento por zona/periodo → batch `COMPLETED` (o `FAILED`).

---

## 🗂 Estructura del repositorio

```
AcusticaUPC/
├── backend/
│   ├── src/main/java/co/edu/unicesar/acustica/
│   │   ├── config/          # Seguridad, CORS, async, cache
│   │   ├── auth/            # JWT, login, usuarios, roles
│   │   ├── zone/            # Zonas y sectores (Res. 0627)
│   │   ├── ingest/          # MeasurementBatch, ingesta POI, @Async
│   │   ├── measurement/     # Measurement, repositorios, consultas por rango
│   │   ├── compliance/      # Evaluación, ComplianceResult, alertas
│   │   ├── dashboard/       # KPIs + agregaciones (con caché)
│   │   ├── report/          # ReportController + CompliancePdfReportService
│   │   └── common/          # Excepciones, DTOs, util fechas (America/Bogota)
│   ├── src/main/resources/
│   │   ├── db/migration/    # Flyway: V1__... ... V6__rename_zone_cempre.sql
│   │   └── application.yml
│   └── pom.xml
│
├── frontend/
│   ├── src/
│   │   ├── lib/             # api.js, auth.js
│   │   ├── ui/              # Sistema de diseño: Rombo, Rings, Card, Badge, Button, charts...
│   │   ├── views/           # Login, Dashboard, Ingesta, Cumplimiento, Usuarios
│   │   ├── app/             # Shell (sidebar/topbar responsive), rutas
│   │   ├── index.css        # Tailwind + animaciones acu-*
│   │   └── main.jsx
│   ├── tailwind.config.js
│   ├── vite.config.js
│   └── package.json
│
├── docs/
│   ├── SPRINT6_PLAN.md
│   ├── SPRINT6_DEV1.md · SPRINT6_DEV2.md · SPRINT6_DEV3.md
│   └── Resolucion0627de2006.pdf
└── README.md
```

---

## 🧩 Modelo de dominio

| Entidad | Campos clave |
|---------|--------------|
| **User** | id, username, email, passwordHash, role (`ADMIN`/`ANALYST`/`VIEWER`) |
| **Zone** | id, name, sector, subsector, estándar día, estándar noche |
| **MeasurementBatch** | id, fileName, zone, status (`PENDING`/`PROCESSING`/`COMPLETED`/`FAILED`), totalRows, validRows, rejectedRows, uploadedAt, processedAt, uploadedBy, errorMessage |
| **Measurement** | id, batch, zone, dbValue (LAeq), measuredAt (`OffsetDateTime`), period (`DIURNO`/`NOCTURNO`) |
| **ComplianceResult** | id, zone, period, laeq, standard, estado (`CUMPLE`/`EXCEDE`) |
| **Alert** | id, zone, period, medido, estándar, exceso, severidad (`LEVE`/`MODERADA`/`CRITICA`), disparada |

Horarios (Res. 0627, Art. 2): **Diurno** 07:01–21:00 · **Nocturno** 21:01–07:00.

---

## 📏 Estándares Resolución 0627 de 2006

Evaluación contra la **Tabla 2 — ruido ambiental** (dB(A)). Subsectores aplicables al campus:

| Sector / Subsector | Día | Noche |
|--------------------|:---:|:-----:|
| B — Universidades, colegios, centros de estudio e investigación | **65** | **50** |
| C — Zonas con usos de oficinas / institucionales | **65** | **50** |

> El indicador es el **LAeq,T** ponderado A. Una zona **EXCEDE** cuando su LAeq del periodo supera el estándar del subsector.

---

## 🔐 Roles y permisos

| Acción | ADMIN | ANALYST | VIEWER |
|--------|:-----:|:-------:|:------:|
| Ver dashboard / cumplimiento | ✅ | ✅ | ✅ |
| Exportar PDF | ✅ | ✅ | ✅ *(habilitado en Sprint 6)* |
| Subir ingesta | ✅ | ✅ | ❌ |
| Reintentar / marcar fallido / eliminar ingesta | ✅ | ❌ | ❌ |
| Gestionar usuarios | ✅ | ❌ | ❌ |

---

## 🚀 Puesta en marcha

### Requisitos
- Java 25 · Maven 3.9+
- Node.js 20+ · npm
- PostgreSQL 15+ (o cadena de conexión de Supabase)

### Backend

```bash
cd backend
cp src/main/resources/application.yml.example src/main/resources/application.yml   # y completa credenciales
./mvnw spring-boot:run
# API en http://localhost:8080/api/v1
```

Compilar artefacto:

```bash
./mvnw clean package
java -jar target/acustica-*.jar
```

### Frontend

```bash
cd frontend
npm install
echo "VITE_API_URL=http://localhost:8080/api/v1" > .env.local
npm run dev
# App en http://localhost:5173
```

Build de producción:

```bash
npm run build && npm run preview
```

### Migraciones

Flyway corre automáticamente al arrancar el backend. Para forzar:

```bash
cd backend && ./mvnw flyway:migrate
```

---

## ⚙️ Variables de entorno

### Backend (`application.yml` / variables de Render)

```yaml
spring:
  datasource:
    url: jdbc:postgresql://<host>:5432/<db>?sslmode=require
    username: ${DB_USER}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate
app:
  jwt:
    secret: ${JWT_SECRET}
    expiration: 86400000          # 24h
  timezone: America/Bogota
  cors:
    allowed-origins: ${FRONTEND_URL}
```

### Frontend (`.env.local` / variables de Vercel)

```
VITE_API_URL=https://<tu-backend>.onrender.com/api/v1
```

---

## 🔌 API REST

Base: `/api/v1`

| Método | Ruta | Rol | Descripción |
|--------|------|-----|-------------|
| `POST` | `/auth/login` | público | Login → JWT |
| `GET` | `/zones` | autenticado | Lista de zonas |
| `GET` | `/dashboard/kpis` | autenticado | KPIs por rango/zona/periodo |
| `GET` | `/dashboard/series` | autenticado | Series para gráficas |
| `GET` | `/compliance` | autenticado | Evaluación + alertas por rango/zona |
| `POST` | `/ingest` | ADMIN/ANALYST | Subir archivo de mediciones |
| `GET` | `/ingest` | autenticado | Historial de cargas |
| `GET` | `/ingest/{id}` | autenticado | Detalle de una carga |
| `POST` | `/ingest/{id}/retry` | ADMIN | Reintentar batch atascado/fallido |
| `POST` | `/ingest/{id}/fail` | ADMIN | Marcar batch como fallido |
| `DELETE` | `/ingest/{id}` | ADMIN | Eliminar batch (cascada + recálculo) |
| `GET` | `/report/pdf` | autenticado | Reporte de cumplimiento en PDF |
| `GET/POST/...` | `/users` | ADMIN | Gestión de usuarios |

Parámetros comunes de consulta: `from` (date), `to` (date, **inclusivo**), `zoneId` (opcional), `period` (`DIURNO`/`NOCTURNO`/`AMBOS`). Las fechas se resuelven en `America/Bogota`, con `to` inclusivo del día completo.

---

## 🔄 Metodología y flujo de trabajo

**Scrum** con sprints quincenales. Equipo de 3 desarrolladores con responsabilidades por capa (backend de dominio, seguridad/API, frontend), apoyándose entre capas cuando una concentra más carga.

### Git Flow

```
main         ← producción (deploy automático Vercel + Render)
  └── develop ← integración del sprint
        └── feature/sN-<descripcion>   ← una rama por tarea
```

- Una rama `feature/*` por tarea; PR a `develop` con al menos 1 revisión.
- Al cierre del sprint: `develop` → `main`.
- **Orden de merge:** primero la fundación de la que dependen otras ramas (p. ej., en el Sprint 6, el sistema de diseño del frontend va primero; ver `docs/SPRINT6_PLAN.md`).

### Convención de commits

```
feat: nueva funcionalidad
fix: corrección de bug
refactor: cambio interno sin alterar comportamiento
docs: documentación
style: formato/estilos
test: pruebas
```

---

## 🗓 Historial de sprints

| Sprint | Foco |
|--------|------|
| 1 | Fundaciones: dominio, modelo de datos, autenticación |
| 2 | Ingesta de archivos (POI) + procesamiento asíncrono + historial |
| 3 | Evaluación de cumplimiento (Res. 0627) + alertas |
| 4 | Dashboard: KPIs, gráficas, mapa de calor |
| 5 | Gestión de usuarios + roles + Export PDF + **despliegue** (Vercel + Render) |
| 6 | **Correcciones** (PDF, fechas, ingestas atascadas, eliminar), **responsive móvil** y **rediseño** |

---

## 👥 Equipo

| Rol | Responsabilidad |
|-----|-----------------|
| **Dev 1** | Backend / ETL · dominio, ingesta, mediciones, cumplimiento |
| **Dev 2** | Backend / Seguridad · autenticación, roles, API REST |
| **Dev 3** | Frontend · React + Vite + Tailwind, sistema de diseño, vistas |

---

<div align="center">
AcústicaUPC · v1.0.0 · Universidad Popular del Cesar · 2026
</div>
