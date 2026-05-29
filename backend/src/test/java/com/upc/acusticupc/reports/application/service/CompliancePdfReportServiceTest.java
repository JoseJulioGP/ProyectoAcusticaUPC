package com.upc.acusticupc.reports.application.service;

import com.upc.acusticupc.reports.domain.dto.ComplianceReportData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompliancePdfReportServiceTest {

    @Mock
    private ComplianceReportDataAssembler assembler;

    private CompliancePdfReportService service;

    @BeforeEach
    void setUp() {
        service = new CompliancePdfReportService(assembler);
    }

    @Test
    void generate_retornaByteArrayConMagicBytesPDF() {
        OffsetDateTime from = OffsetDateTime.now().minusDays(7);
        OffsetDateTime to = OffsetDateTime.now();

        when(assembler.assemble(any(), any(), any())).thenReturn(
                new ComplianceReportData(
                        OffsetDateTime.now(),
                        from, to,
                        "Todas las zonas",
                        100L, 3, 2, 87.5,
                        List.of(), List.of()
                )
        );

        byte[] pdf = service.generate(from, to, null);

        assertTrue(pdf.length > 0);
        assertEquals('%', (char) pdf[0]);
        assertEquals('P', (char) pdf[1]);
        assertEquals('D', (char) pdf[2]);
        assertEquals('F', (char) pdf[3]);
    }

    @Test
    void generate_conListasVacias_noLanzaExcepcion() {
        OffsetDateTime from = OffsetDateTime.now().minusDays(7);
        OffsetDateTime to = OffsetDateTime.now();

        when(assembler.assemble(any(), any(), any())).thenReturn(
                new ComplianceReportData(
                        OffsetDateTime.now(),
                        from, to,
                        "Todas las zonas",
                        0L, 0, 0, 0.0,
                        List.of(), List.of()
                )
        );

        assertDoesNotThrow(() -> service.generate(from, to, null));
    }
}
