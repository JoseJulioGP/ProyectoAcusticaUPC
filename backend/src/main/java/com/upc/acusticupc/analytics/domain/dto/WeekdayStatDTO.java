package com.upc.acusticupc.analytics.domain.dto;

/**
 * Promedio de dB por día de la semana.
 * weekday: EXTRACT(DOW) de Postgres → 0=domingo, 1=lunes, ..., 6=sábado.
 */
public record WeekdayStatDTO(
        int weekday,
        double avgDb,
        long sampleCount
) {}
