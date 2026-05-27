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
               10 * log(avg(power(10, m.db_value / 10.0))) AS laeq_db,
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

    /**
     * Series temporales: LAeq agregado por bucket temporal + zona + periodo.
     * Bucket se construye con date_trunc(:unit, measured_at).
     * unit debe ser un literal SQL: 'hour' o 'day'. Para evitar inyeccion,
     * el llamante valida que viene de TimeBucketer.dateTruncUnit().
     */
    @Query(value = """
    SELECT date_trunc(:unit, m.measured_at)                              AS bucket,
           m.zone_id                                                      AS zone_id,
           10 * log(avg(power(10, m.db_value / 10.0)))                    AS laeq_db,
           count(*)                                                       AS sample_count,
           m.period                                                       AS period
    FROM measurements m
    WHERE m.measured_at BETWEEN :from AND :to
      AND (CAST(:zoneId AS uuid) IS NULL OR m.zone_id = CAST(:zoneId AS uuid))
      AND (:period IS NULL OR m.period = :period)
    GROUP BY bucket, m.zone_id, m.period
    ORDER BY bucket, m.zone_id
    """, nativeQuery = true)
    List<Object[]> laeqTimeSeries(@Param("unit") String unit,
                                  @Param("from") OffsetDateTime from,
                                  @Param("to") OffsetDateTime to,
                                  @Param("zoneId") UUID zoneId,
                                  @Param("period") String period);


    /**
     * Heatmap: LAeq por zona x hora del día (0..23) en el rango.
     * Devuelve [zoneId, hour, laeqDb, sampleCount].
     */
    @Query(value = """
    SELECT m.zone_id                                            AS zone_id,
           EXTRACT(HOUR FROM m.measured_at)::int                AS hour_of_day,
           10 * log(avg(power(10, m.db_value / 10.0)))          AS laeq_db,
           count(*)                                             AS sample_count
    FROM measurements m
    WHERE m.measured_at BETWEEN :from AND :to
      AND (:period IS NULL OR m.period = :period)
    GROUP BY m.zone_id, EXTRACT(HOUR FROM m.measured_at)
    """, nativeQuery = true)
    List<Object[]> laeqByZoneAndHour(@Param("from") OffsetDateTime from,
                                     @Param("to") OffsetDateTime to,
                                     @Param("period") String period);
}