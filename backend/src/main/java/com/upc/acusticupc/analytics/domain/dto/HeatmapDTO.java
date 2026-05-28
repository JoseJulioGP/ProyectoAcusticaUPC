package com.upc.acusticupc.analytics.domain.dto;

import java.util.List;

public record HeatmapDTO(
        List<String> zoneNames,        // eje Y (etiquetas de zonas)
        List<Integer> hours,           // eje X (0..23)
        double[][] laeqMatrix,         // [zoneIndex][hourIndex]; -1 = sin datos
        double[][] exceedanceMatrix    // delta vs estándar; >0 = excede; -1 = sin datos
) {}
