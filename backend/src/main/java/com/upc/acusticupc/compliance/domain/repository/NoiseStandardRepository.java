package com.upc.acusticupc.compliance.domain.repository;

import com.upc.acusticupc.compliance.domain.model.NoiseStandard;
import com.upc.acusticupc.compliance.domain.model.StandardType;
import com.upc.acusticupc.sonometry.domain.model.Period;
import com.upc.acusticupc.zones.domain.model.Sector;
import com.upc.acusticupc.zones.domain.model.Subsector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface NoiseStandardRepository extends JpaRepository<NoiseStandard, UUID> {

    Optional<NoiseStandard> findBySectorAndSubsectorAndPeriodAndStandardType(
        Sector sector, Subsector subsector, Period period, StandardType standardType);
}