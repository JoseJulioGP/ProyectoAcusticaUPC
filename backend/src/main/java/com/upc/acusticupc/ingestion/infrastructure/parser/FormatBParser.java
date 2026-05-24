package com.upc.acusticupc.ingestion.infrastructure.parser;

import com.upc.acusticupc.ingestion.application.dto.ParseResult;
import com.upc.acusticupc.ingestion.domain.model.ExcelFormat;
import com.upc.acusticupc.ingestion.domain.model.ParsedMeasurement;
import com.upc.acusticupc.ingestion.domain.port.SonometerParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Parser para .xlsx con cabecera StartTime/Max/Min/Average/SampleRate.
 * Cada hoja del workbook es una franja del dia (MAÑANA, MEDIO DIA, TARDE).
 * Fechas vienen como strings 'dd-MM-yyyy,HH:mm:ss' en celdas.
 */
@Component
@Slf4j
public class FormatBParser implements SonometerParser {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy,H:mm:ss");
    private static final int DATA_START_ROW = 6;

    @Override
    public ExcelFormat supportedFormat() {
        return ExcelFormat.XLSX_STARTTIME;
    }

    @Override
    public ParseResult parse(InputStream input) throws IOException {
        List<ParsedMeasurement> measurements = new ArrayList<>();
        int rejected = 0;

        try (Workbook workbook = new XSSFWorkbook(input)) {
            for (int sheetIdx = 0; sheetIdx < workbook.getNumberOfSheets(); sheetIdx++) {
                Sheet sheet = workbook.getSheetAt(sheetIdx);
                String sheetName = sheet.getSheetName().trim();
                log.debug("Procesando hoja '{}' ({} filas)", sheetName, sheet.getLastRowNum());

                for (int rowIdx = DATA_START_ROW; rowIdx <= sheet.getLastRowNum(); rowIdx++) {
                    Row row = sheet.getRow(rowIdx);
                    if (row == null) continue;

                    try {
                        Cell unitCell = row.getCell(1);
                        Cell valueCell = row.getCell(2);
                        Cell dateCell = row.getCell(3);
                        if (unitCell == null || valueCell == null || dateCell == null) {
                            rejected++;
                            continue;
                        }
                        String unit = unitCell.getStringCellValue().trim();
                        double dbValue = parseValueCell(valueCell);
                        LocalDateTime capturedAt = LocalDateTime.parse(
                                dateCell.getStringCellValue().trim(), DATE_FORMAT);
                        measurements.add(new ParsedMeasurement(dbValue, unit, capturedAt, sheetName));
                    } catch (Exception e) {
                        log.debug("Fila {} de '{}' ignorada: {}", rowIdx, sheetName, e.getMessage());
                        rejected++;
                    }
                }
            }
        }

        log.info("Formato B parseado: {} mediciones válidas, {} filas ignoradas", measurements.size(), rejected);
        return new ParseResult(measurements, rejected);
    }

    private double parseValueCell(Cell cell) {
        return switch (cell.getCellType()) {
            case NUMERIC -> cell.getNumericCellValue();
            case STRING -> Double.parseDouble(cell.getStringCellValue().trim());
            default -> throw new IllegalArgumentException("Tipo de celda no soportado: " + cell.getCellType());
        };
    }
}
