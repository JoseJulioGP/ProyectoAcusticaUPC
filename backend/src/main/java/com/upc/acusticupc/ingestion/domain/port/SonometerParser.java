package com.upc.acusticupc.ingestion.domain.port;

import com.upc.acusticupc.ingestion.application.dto.ParseResult;
import com.upc.acusticupc.ingestion.domain.model.ExcelFormat;

import java.io.IOException;
import java.io.InputStream;

/**
 * Contrato para parsear un archivo del sonómetro a una lista de mediciones.
 * Implementaciones: TsvSonometerParser, FormatBParser, FormatCParser.
 */
public interface SonometerParser {

    /**
     * @return el formato que esta implementación maneja.
     */
    ExcelFormat supportedFormat();

    /**
     * Parsea el contenido del stream. NO cierra el stream (responsabilidad del llamador).
     */
    ParseResult parse(InputStream input) throws IOException;
}
