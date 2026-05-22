package com.upc.acusticupc.sonometry.domain.model;

import java.time.LocalTime;

/**
 * Periodos del Artículo 2, Resolución 0627/2006.
 *  - DIURNO: 07:01 - 21:00
 *  - NOCTURNO: 21:01 - 07:00
 */
public enum Period {
    DIURNO,
    NOCTURNO;

    private static final LocalTime DIURNO_START = LocalTime.of(7, 1);
    private static final LocalTime DIURNO_END = LocalTime.of(21, 0);

    public static Period fromTime(LocalTime time) {
        if (time == null) {
            throw new IllegalArgumentException("time no puede ser null");
        }
        boolean isDay = !time.isBefore(DIURNO_START) && !time.isAfter(DIURNO_END);
        return isDay ? DIURNO : NOCTURNO;
    }
}