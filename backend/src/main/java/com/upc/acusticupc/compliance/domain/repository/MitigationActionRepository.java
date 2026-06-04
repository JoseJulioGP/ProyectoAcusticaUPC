package com.upc.acusticupc.compliance.domain.repository;

import com.upc.acusticupc.compliance.domain.model.MitigationAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Acceso a {@link MitigationAction}. Sprint 7 — RF#11.
 */
@Repository
public interface MitigationActionRepository extends JpaRepository<MitigationAction, UUID> {

    /** Catálogo activo, ordenado por prioridad ascendente (1 = más relevante). */
    List<MitigationAction> findByActiveTrueOrderByPriorityAsc();

    /** Resuelve por código de negocio (p. ej. {@code "M01"}). */
    Optional<MitigationAction> findByCode(String code);

    /** Atajo para validar unicidad de {@code code} sin traer la entidad. */
    boolean existsByCode(String code);
}
