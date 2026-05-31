package com.upc.acusticupc.reports.infrastructure.web;

import com.upc.acusticupc.reports.application.service.CompliancePdfReportService;
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

    @GetMapping(value = "/compliance/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    public ResponseEntity<byte[]> compliancePdf(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) UUID zoneId) {

        LocalDate t = (to != null)   ? to   : LocalDate.now(DateRangeUtil.BOGOTA);
        LocalDate f = (from != null) ? from : t.minusDays(30);

        DateRangeUtil.DateRange range = DateRangeUtil.resolveRange(f, t);
        byte[] pdf = pdfService.generate(range.start(), range.end(), zoneId);

        String filename = "reporte-cumplimiento-"
                + t.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
