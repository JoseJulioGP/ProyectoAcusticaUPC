package com.upc.acusticupc.shared.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class DateRangeUtilTest {

    @Test
    void resolveRange_incluyeDiaTo_completo() {
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to   = LocalDate.of(2026, 1, 31);

        DateRangeUtil.DateRange range = DateRangeUtil.resolveRange(from, to);

        // Una medición a las 23:30 del día 'to' debe quedar dentro del rango
        OffsetDateTime medicionAlas2330 = LocalDate.of(2026, 1, 31)
                .atTime(23, 30)
                .atZone(DateRangeUtil.BOGOTA)
                .toOffsetDateTime();

        assertThat(medicionAlas2330).isAfterOrEqualTo(range.start());
        assertThat(medicionAlas2330).isBefore(range.end());
    }

    @Test
    void resolveRange_start_esMedianocheDia_from() {
        LocalDate from = LocalDate.of(2026, 3, 15);
        LocalDate to   = LocalDate.of(2026, 3, 15);

        DateRangeUtil.DateRange range = DateRangeUtil.resolveRange(from, to);

        assertThat(range.start().getHour()).isEqualTo(0);
        assertThat(range.start().getMinute()).isEqualTo(0);
        assertThat(range.start().getSecond()).isEqualTo(0);
    }

    @Test
    void resolveRange_end_esMedianocheDia_siguiente() {
        LocalDate from = LocalDate.of(2026, 3, 15);
        LocalDate to   = LocalDate.of(2026, 3, 15);

        DateRangeUtil.DateRange range = DateRangeUtil.resolveRange(from, to);

        // end debe ser el inicio del día 16 (exclusivo)
        OffsetDateTime mediaNocheDia16 = LocalDate.of(2026, 3, 16)
                .atStartOfDay(DateRangeUtil.BOGOTA)
                .toOffsetDateTime();

        assertThat(range.end()).isEqualTo(mediaNocheDia16);
    }

    @Test
    void resolveRange_noIncluyeInicioSiguienteDia() {
        LocalDate from = LocalDate.of(2026, 5, 1);
        LocalDate to   = LocalDate.of(2026, 5, 31);

        DateRangeUtil.DateRange range = DateRangeUtil.resolveRange(from, to);

        // Medianoche del 1 de junio coincide exactamente con el extremo exclusivo
        OffsetDateTime medianoche1Junio = LocalDate.of(2026, 6, 1)
                .atStartOfDay(DateRangeUtil.BOGOTA)
                .toOffsetDateTime();

        assertThat(medianoche1Junio).isEqualTo(range.end());
        // Una medición exactamente en el límite NO se incluye (end exclusivo)
        assertThat(medianoche1Junio).isAfterOrEqualTo(range.end());
    }
}
