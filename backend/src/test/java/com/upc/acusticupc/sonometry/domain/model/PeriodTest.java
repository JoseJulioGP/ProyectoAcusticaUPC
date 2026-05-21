package com.upc.acusticupc.sonometry.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PeriodTest {

    @Test
    void shouldReturnDiurnoForMorningHour() {
        assertEquals(Period.DIURNO, Period.fromTime(LocalTime.of(10, 0)));
    }

    @Test
    void shouldReturnDiurnoFor07h01() {
        assertEquals(Period.DIURNO, Period.fromTime(LocalTime.of(7, 1)));
    }

    @Test
    void shouldReturnNocturnoFor21h01() {
        assertEquals(Period.NOCTURNO, Period.fromTime(LocalTime.of(21, 1)));
    }

    @Test
    void shouldReturnNocturnoForMidnight() {
        assertEquals(Period.NOCTURNO, Period.fromTime(LocalTime.of(0, 0)));
    }

    @Test
    void shouldThrowWhenTimeIsNull() {
        assertThrows(IllegalArgumentException.class, () -> Period.fromTime(null));
    }
}