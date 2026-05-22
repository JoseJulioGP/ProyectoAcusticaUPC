package com.upc.acusticupc.auth.domain.model;

/**
 * Roles de acceso del sistema.
 * - ADMIN: gestiona zonas, usuarios, cargas masivas y reglas legales.
 * - ANALYST: visualiza dashboard y exporta reportes.
 * - VIEWER: solo lectura del dashboard público.
 */
public enum Role {
    ADMIN,
    ANALYST,
    VIEWER
}