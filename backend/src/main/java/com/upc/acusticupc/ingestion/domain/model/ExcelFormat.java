package com.upc.acusticupc.ingestion.domain.model;

/**
 * Formatos de archivo del sonómetro detectados en los datos de la UPC.
 * <ul>
 *   <li>{@link #TSV_STARTTIME}: archivo .xls que en realidad es TSV con cabecera StartTime/Max/Min/Average/SampleRate.</li>
 *   <li>{@link #XLSX_STARTTIME}: archivo .xlsx con la misma cabecera, una hoja por franja del día (MAÑANA, MEDIO DIA, TARDE).</li>
 *   <li>{@link #XLSX_TABULAR}: archivo .xlsx con columnas Place/Date/Time/Value/Unit, opcionalmente con fila título de oficina.</li>
 * </ul>
 */
public enum ExcelFormat {
    TSV_STARTTIME,
    XLSX_STARTTIME,
    XLSX_TABULAR
}
