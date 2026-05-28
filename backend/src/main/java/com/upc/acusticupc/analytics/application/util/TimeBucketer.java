package com.upc.acusticupc.analytics.application.util;

import com.upc.acusticupc.analytics.domain.dto.Granularity;

public final class TimeBucketer {

    private TimeBucketer() {}

    /**
     * Devuelve la funcion SQL date_trunc correspondiente a la granularidad.
     */
    public static String dateTruncUnit(Granularity g) {
        return switch (g) {
            case HOUR -> "hour";
            case DAY -> "day";
        };
    }
}
