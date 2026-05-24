package com.upc.acusticupc.sonometry.application.service;

import com.upc.acusticupc.sonometry.domain.model.Period;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PeriodResolverTest {

    private final PeriodResolver resolver = new PeriodResolver();

    @Test
    void sevenOhOneIsDiurno() {
        assertEquals(Period.DIURNO, resolver.resolve(LocalDateTime.of(2026, 5, 23, 7, 1, 0)));
    }

    @Test
    void twentyOneOhZeroIsDiurno() {
        assertEquals(Period.DIURNO, resolver.resolve(LocalDateTime.of(2026, 5, 23, 21, 0, 0)));
    }

    @Test
    void twentyOneOhOneIsNocturno() {
        assertEquals(Period.NOCTURNO, resolver.resolve(LocalDateTime.of(2026, 5, 23, 21, 1, 0)));
    }

    @Test
    void midnightIsNocturno() {
        assertEquals(Period.NOCTURNO, resolver.resolve(LocalDateTime.of(2026, 5, 23, 0, 0, 0)));
    }

    @Test
    void sevenOhZeroIsNocturno() {
        assertEquals(Period.NOCTURNO, resolver.resolve(LocalDateTime.of(2026, 5, 23, 7, 0, 0)));
    }
}
