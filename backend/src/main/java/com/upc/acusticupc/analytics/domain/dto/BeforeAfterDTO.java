package com.upc.acusticupc.analytics.domain.dto;

import java.util.List;

/**
 * Serie diaria de avgDb partida por la fecha pivote: lo previo y lo posterior.
 */
public record BeforeAfterDTO(
        List<DailyAvgDTO> before,
        List<DailyAvgDTO> after
) {}
