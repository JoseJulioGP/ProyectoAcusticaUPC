package com.upc.acusticupc.sonometry.application.service;

import com.upc.acusticupc.sonometry.domain.model.Period;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Resuelve el periodo (DIURNO/NOCTURNO) según los horarios definidos en la
 * Resolución 627/2006, Artículo 2:
 * <ul>
 *   <li>DIURNO: de 07:01 a 21:00 inclusive.</li>
 *   <li>NOCTURNO: de 21:01 a 07:00 inclusive.</li>
 * </ul>
 *
 * Tambien expone la constante de offset de Colombia, usada para convertir
 * los LocalDateTime del parser a OffsetDateTime al persistir.
 */
@Component
public class PeriodResolver {

    /** Offset de Colombia (UTC-5, sin DST). Constante reutilizable en todo el proyecto. */
    public static final ZoneOffset COLOMBIA_OFFSET = ZoneOffset.of("-05:00");

    private static final LocalTime DIURNO_START = LocalTime.of(7, 1);
    private static final LocalTime DIURNO_END = LocalTime.of(21, 0);

    public Period resolve(LocalDateTime capturedAt) {
        LocalTime t = capturedAt.toLocalTime();
        boolean isDiurno = !t.isBefore(DIURNO_START) && !t.isAfter(DIURNO_END);
        return isDiurno ? Period.DIURNO : Period.NOCTURNO;
    }

    public Period resolve(OffsetDateTime capturedAt) {
        return resolve(capturedAt.toLocalDateTime());
    }

    /** Convierte un LocalDateTime del parser a OffsetDateTime con la zona de Colombia. */
    public OffsetDateTime toColombiaOffset(LocalDateTime local) {
        return local.atOffset(COLOMBIA_OFFSET);
    }
}
