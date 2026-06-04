package com.upc.acusticupc.compliance.application.service;

import com.upc.acusticupc.compliance.application.dto.MitigationActionRequest;
import com.upc.acusticupc.compliance.domain.model.MitigationAction;
import com.upc.acusticupc.compliance.domain.repository.MitigationActionRepository;
import com.upc.acusticupc.shared.exception.ResourceNotFoundException;
import com.upc.acusticupc.shared.util.DateRangeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Servicio del catálogo de acciones de mitigación (Sprint 7 — RF#11).
 *
 * <p>Las acciones se "borran" lógicamente con {@code active = false} (soft-delete).</p>
 *
 * <p>El conflicto por {@code code} duplicado se señaliza con
 * {@link IllegalStateException}, que el {@code GlobalExceptionHandler} ya mapea
 * a HTTP 409 (Bloque B, commit #3).</p>
 */
@Service
@RequiredArgsConstructor
public class MitigationActionService {

    private final MitigationActionRepository repository;

    @Transactional(readOnly = true)
    public List<MitigationAction> listActive() {
        return repository.findByActiveTrueOrderByPriorityAsc();
    }

    /**
     * Sugiere acciones según el exceso de dB sobre el límite normativo.
     * <ul>
     *   <li>{@code excessDb} <= 5: top 3 (bajo costo / fácil implementación).</li>
     *   <li>5 &lt; {@code excessDb} <= 10: top 5.</li>
     *   <li>{@code excessDb} > 10: todas las activas (caso crítico).</li>
     * </ul>
     * Las acciones se devuelven ordenadas por {@code priority} ascendente
     * (1 alta — 5 baja).
     */
    @Transactional(readOnly = true)
    public List<MitigationAction> suggest(double excessDb) {
        List<MitigationAction> all = repository.findByActiveTrueOrderByPriorityAsc();
        int limit = excessDb > 10 ? all.size() : (excessDb > 5 ? 5 : 3);
        return all.stream().limit(limit).toList();
    }

    @Transactional
    public MitigationAction create(MitigationActionRequest req) {
        String code = req.code().trim();
        if (repository.existsByCode(code)) {
            throw new IllegalStateException("Ya existe una acción de mitigación con código " + code);
        }
        MitigationAction entity = MitigationAction.builder()
                .code(code)
                .title(req.title().trim())
                .description(req.description())
                .regulationRef(req.regulationRef())
                .priority(req.priority())
                .estimatedImpactDb(req.estimatedImpactDb())
                .active(req.active() != null ? req.active() : Boolean.TRUE)
                .createdAt(now())
                .build();
        return repository.save(entity);
    }

    @Transactional
    public MitigationAction update(UUID id, MitigationActionRequest req) {
        MitigationAction entity = findOr404(id);
        String newCode = req.code().trim();
        if (!entity.getCode().equals(newCode) && repository.existsByCode(newCode)) {
            throw new IllegalStateException("Ya existe una acción de mitigación con código " + newCode);
        }
        entity.setCode(newCode);
        entity.setTitle(req.title().trim());
        entity.setDescription(req.description());
        entity.setRegulationRef(req.regulationRef());
        entity.setPriority(req.priority());
        entity.setEstimatedImpactDb(req.estimatedImpactDb());
        if (req.active() != null) {
            entity.setActive(req.active());
        }
        entity.setUpdatedAt(now());
        return repository.save(entity);
    }

    /** Soft-delete: marca {@code active = false}. La fila se preserva. */
    @Transactional
    public void deactivate(UUID id) {
        MitigationAction entity = findOr404(id);
        entity.setActive(false);
        entity.setUpdatedAt(now());
        repository.save(entity);
    }

    // ---- helpers ----

    private MitigationAction findOr404(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MitigationAction", id));
    }

    private static OffsetDateTime now() {
        return OffsetDateTime.now(DateRangeUtil.BOGOTA);
    }
}
