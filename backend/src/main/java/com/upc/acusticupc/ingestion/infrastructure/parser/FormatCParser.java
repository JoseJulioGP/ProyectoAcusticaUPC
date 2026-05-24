package com.upc.acusticupc.ingestion.infrastructure.parser;

import com.upc.acusticupc.ingestion.application.dto.ParseResult;
import com.upc.acusticupc.ingestion.domain.model.ExcelFormat;
import com.upc.acusticupc.ingestion.domain.model.ParsedMeasurement;
import com.upc.acusticupc.ingestion.domain.port.SonometerParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Parser para .xlsx con columnas Place/Date/Time/Value/Unit.
 * Cada hoja puede ser un día de la semana o una franja.
 * Fechas y horas como tipos datetime nativos de Excel.
 */
@Component
@Slf4j
public class FormatCParser implements SonometerParser {

    @Override
    public ExcelFormat supportedFormat() {
        return ExcelFormat.XLSX_TABULAR;
    }

    @Override
    public ParseResult parse(InputStream input) throws IOException {
        List<ParsedMeasurement> measurements = new ArrayList<>();
        int rejected = 0;

        try (Workbook workbook = new XSSFWorkbook(input)) {
            for (int sheetIdx = 0; sheetIdx < workbook.getNumberOfSheets(); sheetIdx++) {
                Sheet sheet = workbook.getSheetAt(sheetIdx);
                String sheetName = sheet.getSheetName().trim();

                int dataStart = findDataStartRow(sheet);
                if (dataStart < 0) {
                    log.warn("Hoja '{}' sin header reconocible, se ignora", sheetName);
                    continue;
                }

                for (int rowIdx = dataStart; rowIdx <= sheet.getLastRowNum(); rowIdx++) {
                    Row row = sheet.getRow(rowIdx);
                    if (row == null) continue;

                    try {
                        Cell dateCell = row.getCell(1);
                        Cell timeCell = row.getCell(2);
                        Cell valueCell = row.getCell(3);
                        Cell unitCell = row.getCell(4);

                        if (dateCell == null || timeCell == null || valueCell == null) {
                            rejected++;
                            continue;
                        }

                        LocalDate date = dateCell.getLocalDateTimeCellValue().toLocalDate();
                        LocalTime time = timeCell.getLocalDateTimeCellValue().toLocalTime();
                        LocalDateTime capturedAt = LocalDateTime.of(date, time);
                        double dbValue = valueCell.getNumericCellValue();
                        String unit = unitCell != null ? unitCell.getStringCellValue().trim() : "dB";

                        measurements.add(new ParsedMeasurement(dbValue, unit, capturedAt, sheetName));
                    } catch (Exception e) {
                        log.debug("Fila {} de '{}' ignorada: {}", rowIdx, sheetName, e.getMessage());
                        rejected++;
                    }
                }
            }
        }

        log.info("Formato C parseado: {} mediciones válidas, {} filas ignoradas", measurements.size(), rejected);
        return new ParseResult(measurements, rejected);
    }

    /**
     * Encuentra la fila donde empiezan los datos. Busca una fila cuyo A0 sea "Place".
     * Devuelve el indice de la fila siguiente (donde empiezan los datos), o -1 si no se encuentra.
     */
    private int findDataStartRow(Sheet sheet) {
        for (int i = 0; i <= Math.min(5, sheet.getLastRowNum()); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            Cell cell = row.getCell(0);
            if (cell != null && cell.getCellType() == CellType.STRING) {
                String val = cell.getStringCellValue().trim();
                if (val.equalsIgnoreCase("Place")) {
                    return i + 1;
                }
            }
        }
        return -1;
    }
}
