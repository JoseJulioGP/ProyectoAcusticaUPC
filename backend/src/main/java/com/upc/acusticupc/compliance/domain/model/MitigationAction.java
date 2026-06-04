package com.upc.acusticupc.compliance.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Acción de mitigación de ruido (Sprint 7 — RF#11).
 *
 * <p>Catálogo de medidas técnicas o administrativas para reducir ruido, alineadas
 * con la Resolución 0627 de 2006. El campo {@code priority} (1 alta — 5 baja)
 * permite ordenar por relevancia operativa; {@code estimatedImpactDb} es el
 * delta esperado de reducción en dB(A); {@code regulationRef} apunta al
 * artículo/anexo de la Res. 627 que respalda la acción.</p>
 *
 * <p>El borrado es lógico: {@code active = false} marca una acción retirada
 * del catálogo sin perder histórico.</p>
 */
@Entity
@Table(name = "mitigation_actions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MitigationAction {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true, length = 16)
    private String code;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "regulation_ref", length = 120)
    private String regulationRef;

    /** 1 (alta) – 5 (baja). */
    @Column(nullable = false)
    private Integer priority;

    @Column(name = "estimated_impact_db")
    private Double estimatedImpactDb;

    @Column(nullable = false)
    private Boolean active;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
