package com.upc.acusticupc.ingestion.infrastructure.parser;

import com.upc.acusticupc.ingestion.application.dto.ParseResult;
import com.upc.acusticupc.ingestion.domain.model.ExcelFormat;
import com.upc.acusticupc.ingestion.domain.model.ParsedMeasurement;
import com.upc.acusticupc.ingestion.domain.port.SonometerParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Parser para archivos .xls que son TSV disfrazados (export crudo del sonometro).
 * Formato esperado:
 *   StartTime    dd-MM-yyyy,HH:mm:ss
 *   Max          XX.XX @ ... dBA
 *   Min          XX.XX @ ... dBA
 *   Average      XX.XX
 *   SampleRate   X.XX
 *   ID    Unit  Value  DateTime
 *   1     dBA   54.60  14-04-2026,20:15:43
 */
@Component
@Slf4j
public class TsvSonometerParser implements SonometerParser {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy,H:mm:ss");
    private static final int HEADER_LINES = 6; // 5 metadata + 1 columna headers

    @Override
    public ExcelFormat supportedFormat() {
        return ExcelFormat.TSV_STARTTIME;
    }

    @Override
    public ParseResult parse(InputStream input) throws IOException {
        List<ParsedMeasurement> measurements = new ArrayList<>();
        int rejected = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(input, StandardCharsets.UTF_8))) {

            // Saltar las 6 líneas de cabecera
            for (int i = 0; i < HEADER_LINES; i++) {
                String headerLine = reader.readLine();
                if (headerLine == null) {
                    throw new IOException("Archivo TSV truncado en la cabecera");
                }
            }

            String line;
            int lineNumber = HEADER_LINES;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }
                String[] parts = line.split("\t");
                if (parts.length < 4) {
                    rejected++;
                    continue;
                }
                try {
                    String unit = parts[1].trim();
                    double dbValue = Double.parseDouble(parts[2].trim());
                    LocalDateTime capturedAt = LocalDateTime.parse(parts[3].trim(), DATE_FORMAT);
                    measurements.add(new ParsedMeasurement(dbValue, unit, capturedAt, "default"));
                } catch (Exception e) {
                    log.debug("Línea {} ignorada: {}", lineNumber, e.getMessage());
                    rejected++;
                }
            }
        }

        log.info("TSV parseado: {} mediciones válidas, {} líneas ignoradas", measurements.size(), rejected);
        return new ParseResult(measurements, rejected);
    }
}