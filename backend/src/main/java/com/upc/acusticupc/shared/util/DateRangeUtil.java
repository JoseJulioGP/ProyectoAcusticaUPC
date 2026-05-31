package com.upc.acusticupc.shared.util;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;

public final class DateRangeUtil {

    public static final ZoneId BOGOTA = ZoneId.of("America/Bogota");

    private DateRangeUtil() {}

    public record DateRange(OffsetDateTime start, OffsetDateTime end) {}

    /**
     * Convierte un rango de fechas locales a OffsetDateTime usando la zona de Bogotá.
     * El extremo end es EXCLUSIVO: apunta al inicio del día siguiente a to,
     * de modo que se incluye todo el día to (hasta las 23:59:59.999).
     */
    public static DateRange resolveRange(LocalDate from, LocalDate to) {
        OffsetDateTime start = from.atStartOfDay(BOGOTA).toOffsetDateTime();
        OffsetDateTime end   = to.plusDays(1).atStartOfDay(BOGOTA).toOffsetDateTime();
        return new DateRange(start, end);
    }
}
