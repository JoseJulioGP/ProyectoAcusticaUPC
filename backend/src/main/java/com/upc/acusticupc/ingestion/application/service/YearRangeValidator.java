package com.upc.acusticupc.ingestion.application.service;

import com.upc.acusticupc.ingestion.domain.model.ParsedMeasurement;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Rechaza mediciones cuya fecha capturada por el sonometro esta fuera de rango razonable.
 *
 * Motivacion: el archivo #3-14-4-26.xls (Sprint 2) tenia fechas de 2007 porque el reloj
 * del sonometro estaba descalibrado. El sistema acepto silenciosamente esas mediciones,
 * lo cual contamina cualquier reporte cronologico futuro.
 */
@Component
public class YearRangeValidator {

    private static final int MIN_VALID_YEAR = 2020;

    public boolean isValid(ParsedMeasurement measurement) {
        LocalDateTime capturedAt = measurement.capturedAt();
        return capturedAt != null && capturedAt.getYear() >= MIN_VALID_YEAR;
    }

    public String rejectionReason() {
        return "Fecha fuera de rango valido (anio < " + MIN_VALID_YEAR +
               ") - revisar sincronizacion del sonometro";
    }
}