package com.upc.acusticupc.ingestion.application.service;

import com.upc.acusticupc.ingestion.domain.model.ExcelFormat;
import com.upc.acusticupc.shared.exception.DomainException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Detecta el formato del archivo sin consumirlo (deja la posición en 0).
 * Estrategia:
 *   1. Lee los primeros bytes para distinguir XLSX (magic bytes PK) de TSV (texto).
 *   2. Para TSV: lee la primera línea y mira si empieza con "StartTime".
 *   3. Para XLSX: el orquestador llama refineXlsxFormat(celdaA1) para distinguir B vs C.
 */
@Component
public class ExcelFormatDetector {

    // Magic bytes de un ZIP (XLSX es un ZIP)
    private static final byte[] ZIP_MAGIC = {0x50, 0x4B, 0x03, 0x04};

    /**
     * Detecta si es TSV o XLSX. El InputStream DEBE soportar mark/reset
     * (envolverlo en BufferedInputStream antes de llamar).
     */
    public ExcelFormat detect(InputStream rawInput) throws IOException {
        if (!rawInput.markSupported()) {
            throw new IllegalArgumentException("InputStream must support mark/reset (wrap with BufferedInputStream)");
        }

        rawInput.mark(8);
        byte[] header = new byte[4];
        int read = rawInput.read(header);
        rawInput.reset();

        if (read < 4) {
            throw new DomainException("Archivo demasiado corto para ser un export del sonómetro");
        }

        boolean isZip = header[0] == ZIP_MAGIC[0] && header[1] == ZIP_MAGIC[1]
                && header[2] == ZIP_MAGIC[2] && header[3] == ZIP_MAGIC[3];

        if (!isZip) {
            return detectTsv(rawInput);
        }
        // Para XLSX, devolvemos uno provisional; el orquestador refina mirando A1.
        return ExcelFormat.XLSX_STARTTIME;
    }

    /**
     * Refina el subformato XLSX leyendo la celda A1 de la primera hoja.
     * Solo llamar después de saber que es un XLSX.
     */
    public ExcelFormat refineXlsxFormat(String firstCellA1) {
        if (firstCellA1 == null) {
            throw new DomainException("Archivo XLSX vacío o sin celda A1");
        }
        String normalized = firstCellA1.trim();
        if (normalized.equalsIgnoreCase("StartTime")) {
            return ExcelFormat.XLSX_STARTTIME;
        }
        if (normalized.equalsIgnoreCase("Place")) {
            return ExcelFormat.XLSX_TABULAR;
        }
        // Si A1 es un nombre largo (título de oficina), asumimos tabular con fila título.
        if (normalized.length() > 20) {
            return ExcelFormat.XLSX_TABULAR;
        }
        throw new DomainException("Formato XLSX no reconocido. Celda A1: '" + normalized + "'");
    }

    private ExcelFormat detectTsv(InputStream input) throws IOException {
        input.mark(256);
        byte[] buf = new byte[128];
        int read = input.read(buf);
        input.reset();
        if (read < 1) {
            throw new DomainException("Archivo de texto vacío");
        }
        String firstLine = new String(buf, 0, read, StandardCharsets.UTF_8);
        if (firstLine.startsWith("StartTime")) {
            return ExcelFormat.TSV_STARTTIME;
        }
        throw new DomainException("Archivo de texto no reconocido como TSV del sonómetro");
    }
}
