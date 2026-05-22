package com.upc.acusticupc.zones.domain.repository;

import com.upc.acusticupc.zones.domain.model.Sector;
import com.upc.acusticupc.zones.domain.model.Subsector;
import com.upc.acusticupc.zones.domain.model.Zone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ZoneRepository extends JpaRepository<Zone, UUID> {

    List<Zone> findByActiveTrue();

    List<Zone> findBySectorAndSubsector(Sector sector, Subsector subsector);
}