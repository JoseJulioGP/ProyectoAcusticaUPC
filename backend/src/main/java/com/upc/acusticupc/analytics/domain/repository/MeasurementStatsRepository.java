package com.upc.acusticupc.analytics.domain.repository;

import com.upc.acusticupc.sonometry.domain.model.Measurement;
import com.upc.acusticupc.sonometry.domain.model.Period;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface MeasurementStatsRepository extends JpaRepository<Measurement, UUID> {

    /**
     * Cuenta mediciones en rango (con filtros opcionales).
     */
    @Query("""
        SELECT COUNT(m) FROM Measurement m
        WHERE m.measuredAt BETWEEN :from AND :to
          AND (:zoneId IS NULL OR m.zone.id = :zoneId)
          AND (:period IS NULL OR m.period = :period)
        """)
    long countInRange(@Param("from") OffsetDateTime from,
                      @Param("to") OffsetDateTime to,
                      @Param("zoneId") UUID zoneId,
                      @Param("period") Period period);

    /**
     * Cuántas zonas distintas tienen al menos una medición en el rango.
     */
    @Query("""
        SELECT COUNT(DISTINCT m.zone.id) FROM Measurement m
        WHERE m.measuredAt BETWEEN :from AND :to
        """)
    int countActiveZones(@Param("from") OffsetDateTime from,
                         @Param("to") OffsetDateTime to);

    /**
     * LAeq por zona y período en rango. Devuelve [zoneId, period, laeqDb, sampleCount].
     *
     * Fórmula LAeq: 10 * log10( avg(10^(dB/10)) )
     * En SQL nativo: 10 * log(10, avg(power(10, db_value/10)))
     */
    @Query(value = """
        SELECT m.zone_id              AS zone_id,
               m.period               AS period,
               10 * log(10, avg(power(10, m.db_value / 10.0))) AS laeq_db,
               count(*)               AS sample_count
        FROM measurements m
        WHERE m.measured_at BETWEEN :from AND :to
          AND (CAST(:zoneId AS uuid) IS NULL OR m.zone_id = CAST(:zoneId AS uuid))
        GROUP BY m.zone_id, m.period
        """, nativeQuery = true)
    List<Object[]> laeqByZoneAndPeriod(@Param("from") OffsetDateTime from,
                                       @Param("to") OffsetDateTime to,
                                       @Param("zoneId") UUID zoneId);

    /**
     * Conteo de mediciones por zona (para ZoneStatsDTO.measurements).
     */
    @Query(value = """
        SELECT m.zone_id, count(*)
        FROM measurements m
        WHERE m.measured_at BETWEEN :from AND :to
        GROUP BY m.zone_id
        """, nativeQuery = true)
    List<Object[]> measurementCountByZone(@Param("from") OffsetDateTime from,
                                          @Param("to") OffsetDateTime to);
}