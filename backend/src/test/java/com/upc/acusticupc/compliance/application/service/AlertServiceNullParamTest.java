package com.upc.acusticupc.compliance.application.service;

import com.upc.acusticupc.compliance.domain.model.AlertSeverity;
import com.upc.acusticupc.compliance.domain.repository.AlertRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Sprint 8 fix · {@link AlertService#findAlerts} debe traducir el enum
 * {@link AlertSeverity} a String para que {@code AlertRepository.search}
 * (nativa con {@code CAST(:severity AS text)}) reciba un tipo que Postgres
 * pueda usar incluso cuando llega {@code null}.
 *
 * <p>H2 no reproduce el bug original ("could not determine data type"); estos
 * tests blindan la conversión a nivel de servicio para que la regresión no
 * vuelva sin que nadie lo note.</p>
 */
@ExtendWith(MockitoExtension.class)
class AlertServiceNullParamTest {

    @Mock
    private AlertRepository alertRepository;

    @InjectMocks
    private AlertService alertService;

    private final OffsetDateTime from = OffsetDateTime.parse("2026-04-01T00:00:00-05:00");
    private final OffsetDateTime to   = OffsetDateTime.parse("2026-04-30T23:59:59-05:00");

    @Test
    void findAlerts_conSeverityNull_pasaStringNullAlRepo() {
        @SuppressWarnings("unchecked")
        ArgumentCaptor<String> sevCaptor = ArgumentCaptor.forClass(String.class);
        when(alertRepository.search(any(), any(), any(), sevCaptor.capture(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        alertService.findAlerts(null, from, to, null, Pageable.unpaged());

        assertThat(sevCaptor.getValue()).isNull();
    }

    @Test
    void findAlerts_conSeverityCRITICA_pasaStringCRITICAAlRepo() {
        ArgumentCaptor<String> sevCaptor = ArgumentCaptor.forClass(String.class);
        when(alertRepository.search(any(), any(), any(), sevCaptor.capture(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        alertService.findAlerts(null, from, to, AlertSeverity.CRITICA, Pageable.unpaged());

        assertThat(sevCaptor.getValue()).isEqualTo("CRITICA");
    }
}
