package com.upc.acusticupc.reports.infrastructure.web;

import com.upc.acusticupc.reports.application.service.CompliancePdfReportService;
import com.upc.acusticupc.reports.application.service.DashboardExcelService;
import com.upc.acusticupc.reports.application.service.DashboardPdfService;
import com.upc.acusticupc.shared.util.DateRangeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final CompliancePdfReportService pdfService;
    private final DashboardExcelService excelService;
    private final DashboardPdfService dashboardPdfService;

    private static final String XLSX_MIME =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    @GetMapping(value = "/compliance/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST','VIEWER')")
    public ResponseEntity<byte[]> compliancePdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) UUID zoneId) {

        DateRangeUtil.DateRange range = DateRangeUtil.resolveRange(from, to);
        byte[] pdf = pdfService.generate(range.start(), range.end(), zoneId);

        String filename = "reporte-cumplimiento-"
                + to.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping(value = "/dashboard.xlsx", produces = XLSX_MIME)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> dashboardExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) UUID zoneId) {

        DateRangeUtil.DateRange range = DateRangeUtil.resolveRange(from, to);
        byte[] xlsx = excelService.generate(range.start(), range.end(), zoneId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=dashboard.xlsx")
                .contentType(MediaType.parseMediaType(XLSX_MIME))
                .body(xlsx);
    }

    /**
     * Sprint 8 — Export PDF del dashboard. Mismas secciones que el Excel,
     * formato pensado para lectura/impresión. Rol: cualquier autenticado
     * (incluido VIEWER).
     */
    @GetMapping(value = "/dashboard.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> dashboardPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) UUID zoneId) {

        DateRangeUtil.DateRange range = DateRangeUtil.resolveRange(from, to);
        byte[] pdf = dashboardPdfService.generate(range.start(), range.end(), zoneId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=dashboard.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
