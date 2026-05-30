package com.upc.acusticupc.reports.application.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.upc.acusticupc.reports.domain.dto.ComplianceReportData;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompliancePdfReportService {

    private static final Logger log = LoggerFactory.getLogger(CompliancePdfReportService.class);
    private static final ZoneOffset CO = ZoneOffset.of("-05:00");
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private static final Color UPC_BLUE = new Color(0x1E, 0x3A, 0x5F);
    private static final Color GREY = new Color(0x6B, 0x72, 0x80);
    private static final Color RED = new Color(0xC0, 0x39, 0x2B);
    private static final Color GREEN = new Color(0x27, 0x8A, 0x3A);

    // Inyecta aquí los servicios reales que ya existen. Ejemplo:
    private final ComplianceReportDataAssembler assembler;

    /**
     * Genera el PDF de cumplimiento para el rango/zona dados.
     * @param zoneId null = todas las zonas.
     */
    public byte[] generate(OffsetDateTime from, OffsetDateTime to, UUID zoneId) {
        ComplianceReportData data = assembler.assemble(from, to, zoneId);
        log.info("Generando PDF de cumplimiento: {}..{} zona={} ({} mediciones)",
                from, to, data.zoneFilterLabel(), data.totalMeasurements());

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 48, 48, 54, 54);
            PdfWriter.getInstance(doc, out);
            doc.open();

            addHeader(doc, data);
            addKpis(doc, data);
            addZoneTable(doc, data);
            addAlerts(doc, data);
            addFooter(doc);

            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Fallo generando el PDF de cumplimiento", e);
            throw new IllegalStateException("No se pudo generar el reporte PDF", e);
        }
    }

    private void addHeader(Document doc, ComplianceReportData d) throws DocumentException {
        Font title = new Font(Font.HELVETICA, 20, Font.BOLD, UPC_BLUE);
        Font sub = new Font(Font.HELVETICA, 11, Font.NORMAL, GREY);

        Paragraph t = new Paragraph("Reporte de Cumplimiento Acústico", title);
        t.setSpacingAfter(2);
        doc.add(t);
        doc.add(new Paragraph("Resolución 0627 de 2006 — MAVDT", sub));
        doc.add(new Paragraph("Universidad Popular del Cesar — AcústicaUPC", sub));
        doc.add(Chunk.NEWLINE);

        Font meta = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.BLACK);
        doc.add(new Paragraph("Zona: " + d.zoneFilterLabel(), meta));
        doc.add(new Paragraph("Periodo: " + d.from().withOffsetSameInstant(CO).format(FMT)
                + "  a  " + d.to().withOffsetSameInstant(CO).format(FMT), meta));
        doc.add(new Paragraph("Generado: " + d.generatedAt().withOffsetSameInstant(CO).format(FMT)
                + " (hora Colombia)", meta));
        doc.add(Chunk.NEWLINE);
        addSeparator(doc);
    }

    private void addKpis(Document doc, ComplianceReportData d) throws DocumentException {
        doc.add(sectionTitle("Indicadores generales"));
        PdfPTable t = new PdfPTable(4);
        t.setWidthPercentage(100);
        t.setSpacingBefore(6);
        t.addCell(kpiCell("Mediciones", String.valueOf(d.totalMeasurements())));
        t.addCell(kpiCell("Zonas activas", String.valueOf(d.activeZones())));
        t.addCell(kpiCell("Alertas", String.valueOf(d.totalAlerts())));
        t.addCell(kpiCell("% Cumplimiento", String.format("%.1f%%", d.complianceRatePercent())));
        doc.add(t);
        doc.add(Chunk.NEWLINE);
    }

    private void addZoneTable(Document doc, ComplianceReportData d) throws DocumentException {
        doc.add(sectionTitle("Cumplimiento por zona y periodo"));
        PdfPTable t = new PdfPTable(new float[]{3, 2, 2, 2, 2});
        t.setWidthPercentage(100);
        t.setSpacingBefore(6);
        for (String h : new String[]{"Zona", "Periodo", "LAeq (dB)", "Estándar (dB)", "Estado"}) {
            t.addCell(headerCell(h));
        }
        if (d.zoneRows().isEmpty()) {
            PdfPCell empty = new PdfPCell(new Phrase("Sin datos en el rango seleccionado.",
                    new Font(Font.HELVETICA, 9, Font.ITALIC, GREY)));
            empty.setColspan(5);
            empty.setPadding(8);
            t.addCell(empty);
        } else {
            for (var r : d.zoneRows()) {
                t.addCell(bodyCell(r.zoneName(), false));
                t.addCell(bodyCell(r.period(), true));
                t.addCell(bodyCell(String.format("%.1f", r.laeqDb()), true));
                t.addCell(bodyCell(String.format("%.0f", r.standardDb()), true));
                boolean ok = "CUMPLE".equalsIgnoreCase(r.status());
                t.addCell(statusCell(r.status(), ok ? GREEN : RED));
            }
        }
        doc.add(t);
        doc.add(Chunk.NEWLINE);
    }

    private void addAlerts(Document doc, ComplianceReportData d) throws DocumentException {
        doc.add(sectionTitle("Alertas (" + d.alerts().size() + ")"));
        if (d.alerts().isEmpty()) {
            doc.add(new Paragraph("No se registraron alertas en el periodo.",
                    new Font(Font.HELVETICA, 9, Font.ITALIC, GREY)));
            return;
        }
        PdfPTable t = new PdfPTable(new float[]{3, 2, 2, 2, 2, 3});
        t.setWidthPercentage(100);
        t.setSpacingBefore(6);
        for (String h : new String[]{"Zona", "Periodo", "LAeq", "Estándar", "Severidad", "Fecha"}) {
            t.addCell(headerCell(h));
        }
        for (var a : d.alerts()) {
            t.addCell(bodyCell(a.zoneName(), false));
            t.addCell(bodyCell(a.period(), true));
            t.addCell(bodyCell(String.format("%.1f", a.laeqDb()), true));
            t.addCell(bodyCell(String.format("%.0f", a.standardDb()), true));
            t.addCell(bodyCell(a.severity(), true));
            t.addCell(bodyCell(a.triggeredAt().withOffsetSameInstant(CO).format(FMT), true));
        }
        doc.add(t);
    }

    private void addFooter(Document doc) throws DocumentException {
        doc.add(Chunk.NEWLINE);
        addSeparator(doc);
        Font f = new Font(Font.HELVETICA, 8, Font.NORMAL, GREY);
        doc.add(new Paragraph(
                "Documento generado automáticamente por AcústicaUPC. "
                + "Los niveles se comparan contra los estándares máximos permisibles "
                + "de la Tabla 2 (ruido ambiental) de la Resolución 0627 de 2006.", f));
    }

    // ---- helpers de estilo ----

    private Paragraph sectionTitle(String s) {
        Paragraph p = new Paragraph(s, new Font(Font.HELVETICA, 13, Font.BOLD, UPC_BLUE));
        p.setSpacingBefore(8);
        return p;
    }

    private void addSeparator(Document doc) throws DocumentException {
        PdfPTable line = new PdfPTable(1);
        line.setWidthPercentage(100);
        PdfPCell c = new PdfPCell();
        c.setBorder(Rectangle.BOTTOM);
        c.setBorderColor(new Color(0xE0, 0xE0, 0xE0));
        c.setFixedHeight(2);
        line.addCell(c);
        doc.add(line);
    }

    private PdfPCell kpiCell(String label, String value) {
        PdfPCell c = new PdfPCell();
        c.setPadding(8);
        c.setBackgroundColor(new Color(0xF4, 0xF6, 0xF8));
        c.setBorderColor(new Color(0xE0, 0xE0, 0xE0));
        c.addElement(new Paragraph(label, new Font(Font.HELVETICA, 8, Font.NORMAL, GREY)));
        c.addElement(new Paragraph(value, new Font(Font.HELVETICA, 16, Font.BOLD, UPC_BLUE)));
        return c;
    }

    private PdfPCell headerCell(String s) {
        PdfPCell c = new PdfPCell(new Phrase(s, new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE)));
        c.setBackgroundColor(UPC_BLUE);
        c.setPadding(5);
        return c;
    }

    private PdfPCell bodyCell(String s, boolean center) {
        PdfPCell c = new PdfPCell(new Phrase(s, new Font(Font.HELVETICA, 9, Font.NORMAL, Color.BLACK)));
        c.setPadding(5);
        if (center) c.setHorizontalAlignment(Element.ALIGN_CENTER);
        return c;
    }

    private PdfPCell statusCell(String s, Color color) {
        PdfPCell c = new PdfPCell(new Phrase(s, new Font(Font.HELVETICA, 9, Font.BOLD, color)));
        c.setPadding(5);
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        return c;
    }
}