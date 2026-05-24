package com.upc.acusticupc.ingestion.application.service;

import com.upc.acusticupc.ingestion.domain.model.ExcelFormat;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

class ExcelFormatDetectorTest {

    private final ExcelFormatDetector detector = new ExcelFormatDetector();

    @Test
    void detectsTsvStartTime() throws Exception {
        try (InputStream in = new BufferedInputStream(
                new ClassPathResource("sonometer-samples/format_a.xls").getInputStream())) {
            assertEquals(ExcelFormat.TSV_STARTTIME, detector.detect(in));
        }
    }

    @Test
    void refinesXlsxWhenFirstCellIsStartTime() {
        assertEquals(ExcelFormat.XLSX_STARTTIME, detector.refineXlsxFormat("StartTime"));
    }

    @Test
    void refinesXlsxWhenFirstCellIsPlace() {
        assertEquals(ExcelFormat.XLSX_TABULAR, detector.refineXlsxFormat("Place"));
    }

    @Test
    void refinesXlsxWhenFirstCellIsOfficeTitle() {
        assertEquals(ExcelFormat.XLSX_TABULAR,
                detector.refineXlsxFormat("OFICINA DE SEGURIDAD Y SALUD EN EL TRABAJO"));
    }
}
