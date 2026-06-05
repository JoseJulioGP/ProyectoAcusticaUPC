package com.upc.acusticupc.reports.application.service;

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
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Genera el Excel del dashboard con 6 hojas reutilizando los servicios de
 * analítica y alertas existentes. No recalcula nada por su cuenta.
 */
@Service
@RequiredArgsConstructor
public class DashboardExcelService {

    private final KpiService kpiService;
    private final TimeSeriesService timeSeriesService;
    private final ZoneStatsService zoneStatsService;
    private final AlertService alertService;
    private final MeasurementBatchRepository batchRepository;

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Transactional(readOnly = true)
    public byte[] generate(OffsetDateTime from, OffsetDateTime to, UUID zoneId) {
        try (Workbook wb = new XSSFWorkbook()) {
            CellStyle header = headerStyle(wb);

            buildResumen(wb, from, to, zoneId);
            buildSeries(wb, header, "Por hora",
                    timeSeriesService.series(from, to, zoneId, null, Granularity.HOUR));
            buildSeries(wb, header, "Por día",
                    timeSeriesService.series(from, to, zoneId, null, Granularity.DAY));
            buildPorZona(wb, header, from, to);
            buildNoConformidades(wb, header, from, to, zoneId);
            buildObservaciones(wb, header);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error generando el Excel del dashboard", e);
        }
    }

    private void buildResumen(Workbook wb, OffsetDateTime from, OffsetDateTime to, UUID zoneId) {
        DashboardKpisDTO k = kpiService.computeKpis(from, to, zoneId, null);
        Sheet s = wb.createSheet("Resumen");
        int r = 0;
        r = kv(s, r, "Desde", from != null ? from.format(TS) : "");
        r = kv(s, r, "Hasta", to != null ? to.format(TS) : "");
        r = kv(s, r, "Mediciones totales", String.valueOf(k.totalMeasurements()));
        r = kv(s, r, "Zonas activas", String.valueOf(k.zonesActive()));
        r = kv(s, r, "Alertas activas", String.valueOf(k.alertsActive()));
        kv(s, r, "% cumplimiento", String.format("%.1f", k.complianceRatePercent()));
        autosize(s, 2);
    }

    private void buildSeries(Workbook wb, CellStyle header, String name, List<TimeSeriesPointDTO> points) {
        Sheet s = wb.createSheet(name);
        headerRow(s, header, "Fecha", "Zona", "LAeq dB", "Muestras", "Periodo");
        int r = 1;
        for (TimeSeriesPointDTO p : points) {
            Row row = s.createRow(r++);
            row.createCell(0).setCellValue(p.bucket() != null ? p.bucket().format(TS) : "");
            row.createCell(1).setCellValue(p.zoneName());
            row.createCell(2).setCellValue(p.laeqDb());
            row.createCell(3).setCellValue(p.sampleCount());
            row.createCell(4).setCellValue(p.period() != null ? p.period().name() : "");
        }
        autosize(s, 5);
    }

    private void buildPorZona(Workbook wb, CellStyle header, OffsetDateTime from, OffsetDateTime to) {
        List<ZoneStatsDTO> stats = zoneStatsService.statsForAllZones(from, to);
        Sheet s = wb.createSheet("Por zona");
        headerRow(s, header, "Zona", "Sector", "Subsector", "Mediciones",
                "LAeq Diurno", "LAeq Nocturno", "Estándar día", "Estándar noche", "Alertas", "Estado");
        int r = 1;
        for (ZoneStatsDTO z : stats) {
            Row row = s.createRow(r++);
            row.createCell(0).setCellValue(z.zoneName());
            row.createCell(1).setCellValue(z.sector());
            row.createCell(2).setCellValue(z.subsector());
            row.createCell(3).setCellValue(z.measurements());
            if (z.laeqDiurnoDb() != null) row.createCell(4).setCellValue(z.laeqDiurnoDb());
            if (z.laeqNocturnoDb() != null) row.createCell(5).setCellValue(z.laeqNocturnoDb());
            row.createCell(6).setCellValue(z.standardDayDb());
            row.createCell(7).setCellValue(z.standardNightDb());
            row.createCell(8).setCellValue(z.alertsCount());
            row.createCell(9).setCellValue(z.overallStatus() != null ? z.overallStatus().name() : "");
        }
        autosize(s, 10);
    }

    private void buildNoConformidades(Workbook wb, CellStyle header,
                                      OffsetDateTime from, OffsetDateTime to, UUID zoneId) {
        List<AlertResponse> alerts = alertService
                .findAlerts(zoneId, from, to, null, Pageable.unpaged()).getContent();
        Sheet s = wb.createSheet("No conformidades");
        headerRow(s, header, "Zona", "Fecha", "Periodo", "Medido dB", "Estándar dB", "Exceso dB", "Severidad");
        int r = 1;
        for (AlertResponse a : alerts) {
            Row row = s.createRow(r++);
            row.createCell(0).setCellValue(a.zoneName());
            row.createCell(1).setCellValue(a.when() != null ? a.when().format(TS) : "");
            row.createCell(2).setCellValue(a.period());
            row.createCell(3).setCellValue(a.measuredDb());
            row.createCell(4).setCellValue(a.standardDb());
            row.createCell(5).setCellValue(a.excessDb());
            row.createCell(6).setCellValue(a.severity());
        }
        autosize(s, 7);
    }

    private void buildObservaciones(Workbook wb, CellStyle header) {
        List<MeasurementBatch> batches = batchRepository.findByObservationIsNotNull();
        Sheet s = wb.createSheet("Observaciones");
        headerRow(s, header, "Archivo", "Zona", "Observación");
        int r = 1;
        for (MeasurementBatch b : batches) {
            Row row = s.createRow(r++);
            row.createCell(0).setCellValue(b.getFileName());
            row.createCell(1).setCellValue(b.getZone() != null ? b.getZone().getName() : "");
            row.createCell(2).setCellValue(b.getObservation());
        }
        autosize(s, 3);
    }

    // ----- helpers POI -----

    private CellStyle headerStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private void headerRow(Sheet s, CellStyle style, String... cols) {
        Row row = s.createRow(0);
        for (int i = 0; i < cols.length; i++) {
            Cell c = row.createCell(i);
            c.setCellValue(cols[i]);
            c.setCellStyle(style);
        }
    }

    private int kv(Sheet s, int rowIdx, String key, String value) {
        Row row = s.createRow(rowIdx);
        row.createCell(0).setCellValue(key);
        row.createCell(1).setCellValue(value);
        return rowIdx + 1;
    }

    private void autosize(Sheet s, int cols) {
        for (int i = 0; i < cols; i++) s.autoSizeColumn(i);
    }
}
