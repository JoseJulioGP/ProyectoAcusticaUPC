package com.upc.acusticupc.compliance.application.service;

import com.upc.acusticupc.compliance.application.dto.AlertResponse;
import com.upc.acusticupc.compliance.domain.model.AlertSeverity;
import com.upc.acusticupc.compliance.domain.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository alertRepository;

    @Transactional(readOnly = true)
    public Page<AlertResponse> findAlerts(UUID zoneId, OffsetDateTime from, OffsetDateTime to,
                                          AlertSeverity severity, Pageable pageable) {
        return alertRepository.search(zoneId, from, to, severity, pageable)
                .map(AlertResponse::from);
    }
}
