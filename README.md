# AcústicaUPC

Sistema BI de monitoreo de ruido ambiental UPC bajo Resolución 627/2006.

## Stack

- **Backend:** Spring Boot 4.0.6 + Java 25 + PostgreSQL (Supabase) + Flyway
- **Frontend:** React 18 + Vite + Tailwind CSS 3

## Arranque

```bash
# Backend
cd backend && ./mvnw spring-boot:run

# Frontend
cd frontend && npm run dev
```

## Estructura de ramas

- `main` — producción
- `develop` — integración de sprint
- `feature/sprint1-foundation-domain`
- `feature/sprint1-auth-security`
- `feature/sprint1-frontend-foundation`

## Estructura

```
acusticupc/
├── backend/    Spring Boot (screaming architecture por feature)
├── frontend/   Vite + React (feature folders)
└── docs/       Documentación de planificación
```
