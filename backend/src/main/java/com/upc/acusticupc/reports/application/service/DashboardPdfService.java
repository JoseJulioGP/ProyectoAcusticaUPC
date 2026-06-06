package com.upc.acusticupc.reports.application.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.upc.acusticupc.analytics.application.service.KpiService;
import com.upc.acusticupc.analytics.application.service.TimeSeriesService;
import com.upc.acusticupc.analytics.application.service.ZoneStatsService;
import com.upc.acusticupc.analytics.domain.dto.DashboardKpisDTO;
import com.upc.acusticupc.analytics.domain.dto.Granularity;
import com.upc.acusticupc.analytics.domain.dto.TimeSeriesPointDTO;
import com.upc.acusticupc.analytics.domain.dto.ZoneStatsDTO;
import com.upc.acusticupc.compliance.application.dto.AlertResponse;
import com.upc.acusticupc.compliance.application.service.AlertService;
import com.upc.acusticupc.sonometry.domain.model.MeasurementBatch;
import com.upc.acusticupc.sonometry.domain.repository.MeasurementBatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Sprint 8 — Genera el PDF del dashboard con las mismas 6 secciones del Excel
 * reutilizando los servicios de analítica y alertas. No recalcula nada.
 *
 * <p>Implementado con OpenPDF (LGPL/MPL, ya en el pom del proyecto). Estructura
 * pensada para una lectura rápida (cabeceras, tablas, sin gráficas — para gráficas
 * va el Excel/Vercel).</p>
 */
@Service
@RequiredArgsConstructor
public class DashboardPdfService {

    private final KpiService kpiService;
    private final TimeSeriesService timeSeriesService;
    private final ZoneStatsService zoneStatsService;
    private final AlertService alertService;
    private final MeasurementBatchRepository batchRepository;

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private static final Font TITLE_FONT   = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Color.BLACK);
    private static final Font HEADING_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(19, 105, 74));
    private static final Font BODY_FONT    = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
    private static final Font TABLE_HEAD   = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);

    @Transactional(readOnly = true)
    public byte[] generate(OffsetDateTime from, OffsetDateTime to, UUID zoneId) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document doc = new Document(PageSize.A4.rotate(), 36, 36, 40, 40);
            PdfWriter.getInstance(doc, out);
            doc.open();

            title(doc, "AcústicaUPC · Reporte de dashboard");
            doc.add(new Paragraph(
                    "Rango: " + (from != null ? from.format(TS) : "—") + "  →  " + (to != null ? to.format(TS) : "—"),
                    BODY_FONT));
            spacer(doc);

            sectionResumen(doc, from, to, zoneId);
            sectionSeries(doc, "Por hora",
                    timeSeriesService.series(from, to, zoneId, null, Granularity.HOUR));
            sectionSeries(doc, "Por día",
                    timeSeriesService.series(from, to, zoneId, null, Granularity.DAY));
            sectionPorZona(doc, from, to);
            sectionNoConformidades(doc, from, to, zoneId);
            sectionObservaciones(doc);

            doc.close();
            return out.toByteArray();
        } catch (DocumentException e) {
            throw new RuntimeException("Error generando el PDF del dashboard", e);
        }
    }

    // -------- Secciones --------

    private void sectionResumen(Document doc, OffsetDateTime from, OffsetDateTime to, UUID zoneId)
            throws DocumentException {
        heading(doc, "Resumen (KPIs)");
        DashboardKpisDTO k = kpiService.computeKpis(from, to, zoneId, null);
        PdfPTable t = newTable(2, new int[]{1, 2});
        addCell(t, "Mediciones totales", TABLE_HEAD, primary());
        addCell(t, String.valueOf(k.totalMeasurements()), BODY_FONT, null);
        addCell(t, "Zonas activas", TABLE_HEAD, primary());
        addCell(t, String.valueOf(k.zonesActive()), BODY_FONT, null);
        addCell(t, "Alertas activas", TABLE_HEAD, primary());
        addCell(t, String.valueOf(k.alertsActive()), BODY_FONT, null);
        addCell(t, "% cumplimiento", TABLE_HEAD, primary());
        addCell(t, String.format("%.1f", k.complianceRatePercent()), BODY_FONT, null);
        doc.add(t);
        spacer(doc);
    }

    private void sectionSeries(Document doc, String name, List<TimeSeriesPointDTO> points)
            throws DocumentException {
        heading(doc, name);
        PdfPTable t = newTable(5, new int[]{3, 3, 2, 2, 2});
        for (String h : new String[]{"Fecha", "Zona", "LAeq dB", "Muestras", "Periodo"}) {
            addCell(t, h, TABLE_HEAD, primary());
        }
        for (TimeSeriesPointDTO p : points) {
            addCell(t, p.bucket() != null ? p.bucket().format(TS) : "", BODY_FONT, null);
            addCell(t, p.zoneName(), BODY_FONT, null);
            addCell(t, String.format("%.1f", p.laeqDb()), BODY_FONT, null);
            addCell(t, String.valueOf(p.sampleCount()), BODY_FONT, null);
            addCell(t, p.period() != null ? p.period().name() : "", BODY_FONT, null);
        }
        doc.add(t);
        spacer(doc);
    }

    private void sectionPorZona(Document doc, OffsetDateTime from, OffsetDateTime to)
            throws DocumentException {
        heading(doc, "Por zona");
        List<ZoneStatsDTO> stats = zoneStatsService.statsForAllZones(from, to);
        PdfPTable t = newTable(10, new int[]{3, 2, 3, 2, 2, 2, 2, 2, 2, 2});
        for (String h : new String[]{"Zona", "Sector", "Subsector", "Mediciones",
                "LAeq Diurno", "LAeq Nocturno", "Estándar día", "Estándar noche", "Alertas", "Estado"}) {
            addCell(t, h, TABLE_HEAD, primary());
        }
        for (ZoneStatsDTO z : stats) {
            addCell(t, nz(z.zoneName()), BODY_FONT, null);
            addCell(t, nz(z.sector()), BODY_FONT, null);
            addCell(t, nz(z.subsector()), BODY_FONT, null);
            addCell(t, String.valueOf(z.measurements()), BODY_FONT, null);
            addCell(t, z.laeqDiurnoDb() != null ? String.format("%.1f", z.laeqDiurnoDb()) : "—", BODY_FONT, null);
            addCell(t, z.laeqNocturnoDb() != null ? String.format("%.1f", z.laeqNocturnoDb()) : "—", BODY_FONT, null);
            addCell(t, String.format("%.1f", z.standardDayDb()), BODY_FONT, null);
            addCell(t, String.format("%.1f", z.standardNightDb()), BODY_FONT, null);
            addCell(t, String.valueOf(z.alertsCount()), BODY_FONT, null);
            addCell(t, z.overallStatus() != null ? z.overallStatus().name() : "", BODY_FONT, null);
        }
        doc.add(t);
        spacer(doc);
    }

    private void sectionNoConformidades(Document doc, OffsetDateTime from, OffsetDateTime to, UUID zoneId)
            throws DocumentException {
        heading(doc, "No conformidades");
        List<AlertResponse> alerts = alertService
                .findAlerts(zoneId, from, to, null, Pageable.unpaged()).getContent();
        PdfPTable t = newTable(7, new int[]{3, 3, 2, 2, 2, 2, 2});
        for (String h : new String[]{"Zona", "Fecha", "Periodo", "Medido dB", "Estándar dB", "Exceso dB", "Severidad"}) {
            addCell(t, h, TABLE_HEAD, primary());
        }
        for (AlertResponse a : alerts) {
            addCell(t, nz(a.zoneName()), BODY_FONT, null);
            addCell(t, a.when() != null ? a.when().format(TS) : "", BODY_FONT, null);
            addCell(t, nz(a.period()), BODY_FONT, null);
            addCell(t, String.format("%.1f", a.measuredDb()), BODY_FONT, null);
            addCell(t, String.format("%.1f", a.standardDb()), BODY_FONT, null);
            addCell(t, String.format("%.1f", a.excessDb()), BODY_FONT, null);
            addCell(t, nz(a.severity()), BODY_FONT, null);
        }
        doc.add(t);
        spacer(doc);
    }

    private void sectionObservaciones(Document doc) throws DocumentException {
        heading(doc, "Observaciones");
        List<MeasurementBatch> batches = batchRepository.findByObservationIsNotNull();
        PdfPTable t = newTable(3, new int[]{3, 3, 6});
        for (String h : new String[]{"Archivo", "Zona", "Observación"}) {
            addCell(t, h, TABLE_HEAD, primary());
        }
        for (MeasurementBatch b : batches) {
            addCell(t, nz(b.getFileName()), BODY_FONT, null);
            addCell(t, b.getZone() != null ? nz(b.getZone().getName()) : "", BODY_FONT, null);
            addCell(t, nz(b.getObservation()), BODY_FONT, null);
        }
        doc.add(t);
    }

    // -------- Helpers --------

    private void title(Document doc, String text) throws DocumentException {
        Paragraph p = new Paragraph(text, TITLE_FONT);
        p.setSpacingAfter(8);
        doc.add(p);
    }

    private void heading(Document doc, String text) throws DocumentException {
        Paragraph p = new Paragraph(text, HEADING_FONT);
        p.setSpacingBefore(4);
        p.setSpacingAfter(6);
        doc.add(p);
    }

    private void spacer(Document doc) throws DocumentException {
        doc.add(new Paragraph(" ", BODY_FONT));
    }

    private PdfPTable newTable(int columns, int[] widths) throws DocumentException {
        PdfPTable t = new PdfPTable(columns);
        t.setWidthPercentage(100);
        t.setWidths(widths);
        return t;
    }

    private void addCell(PdfPTable t, String text, Font font, Color bg) {
        PdfPCell cell = new PdfPCell(new Paragraph(text == null ? "" : text, font));
        cell.setPadding(4);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        if (bg != null) cell.setBackgroundColor(bg);
        t.addCell(cell);
    }

    private Color primary() {
        return new Color(19, 105, 74); // petróleo del DS
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }
}
