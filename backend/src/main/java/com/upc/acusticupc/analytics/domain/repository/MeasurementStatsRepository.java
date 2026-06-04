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
     * Rango: from inclusivo, to exclusivo — usar siempre con resolveRange().
     */
    @Query("""
        SELECT COUNT(m) FROM Measurement m
        WHERE m.measuredAt >= :from AND m.measuredAt < :to
          AND (:zoneId IS NULL OR m.zone.id = :zoneId)
          AND (:period IS NULL OR m.period = :period)
        """)
    long countInRange(@Param("from") OffsetDateTime from,
                      @Param("to") OffsetDateTime to,
                      @Param("zoneId") UUID zoneId,
                      @Param("period") Period period);

    /**
     * Cuántas zonas distintas tienen al menos una medición en el rango.
     * Rango exclusivo en el extremo superior.
     */
    @Query("""
        SELECT COUNT(DISTINCT m.zone.id) FROM Measurement m
        WHERE m.measuredAt >= :from AND m.measuredAt < :to
        """)
    int countActiveZones(@Param("from") OffsetDateTime from,
                         @Param("to") OffsetDateTime to);

    /**
     * LAeq por zona y período en rango. Devuelve [zoneId, period, laeqDb, sampleCount].
     * Fórmula LAeq: 10 * log10( avg(10^(dB/10)) ).
     * Rango exclusivo en el extremo superior.
     */
    @Query(value = """
        SELECT m.zone_id              AS zone_id,
               m.period               AS period,
               10 * log(avg(power(10, m.db_value / 10.0))) AS laeq_db,
               count(*)               AS sample_count
        FROM measurements m
        WHERE m.measured_at >= :from AND m.measured_at < :to
          AND (CAST(:zoneId AS uuid) IS NULL OR m.zone_id = CAST(:zoneId AS uuid))
        GROUP BY m.zone_id, m.period
        """, nativeQuery = true)
    List<Object[]> laeqByZoneAndPeriod(@Param("from") OffsetDateTime from,
                                       @Param("to") OffsetDateTime to,
                                       @Param("zoneId") UUID zoneId);

    /**
     * Conteo de mediciones por zona.
     * Rango exclusivo en el extremo superior.
     */
    @Query(value = """
        SELECT m.zone_id, count(*)
        FROM measurements m
        WHERE m.measured_at >= :from AND m.measured_at < :to
        GROUP BY m.zone_id
        """, nativeQuery = true)
    List<Object[]> measurementCountByZone(@Param("from") OffsetDateTime from,
                                          @Param("to") OffsetDateTime to);

    /**
     * Series temporales: LAeq agregado por bucket temporal + zona + periodo.
     * unit debe ser un literal SQL: 'hour' o 'day'. Para evitar inyeccion,
     * el llamante valida que viene de TimeBucketer.dateTruncUnit().
     * Rango exclusivo en el extremo superior.
     */
    @Query(value = """
    SELECT date_trunc(:unit, m.measured_at)                              AS bucket,
           m.zone_id                                                      AS zone_id,
           10 * log(avg(power(10, m.db_value / 10.0)))                    AS laeq_db,
           count(*)                                                       AS sample_count,
           m.period                                                       AS period
    FROM measurements m
    WHERE m.measured_at >= :from AND m.measured_at < :to
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
     * Rango exclusivo en el extremo superior.
     */
    @Query(value = """
    SELECT m.zone_id                                            AS zone_id,
           EXTRACT(HOUR FROM m.measured_at)::int                AS hour_of_day,
           10 * log(avg(power(10, m.db_value / 10.0)))          AS laeq_db,
           count(*)                                             AS sample_count
    FROM measurements m
    WHERE m.measured_at >= :from AND m.measured_at < :to
      AND (:period IS NULL OR m.period = :period)
    GROUP BY m.zone_id, EXTRACT(HOUR FROM m.measured_at)
    """, nativeQuery = true)
    List<Object[]> laeqByZoneAndHour(@Param("from") OffsetDateTime from,
                                     @Param("to") OffsetDateTime to,
                                     @Param("period") String period);

    /**
     * Promedio de dB por día de la semana (EXTRACT(DOW): 0=domingo .. 6=sábado)
     * para un año y rango de semanas ISO. zoneId opcional.
     * Devuelve [dow, avgDb, sampleCount].
     */
    @Query(value = """
        SELECT EXTRACT(DOW FROM m.measured_at)::int                 AS dow,
               avg(m.db_value)                                       AS avg_db,
               count(*)                                              AS sample_count
        FROM measurements m
        WHERE (CAST(:zoneId AS uuid) IS NULL OR m.zone_id = CAST(:zoneId AS uuid))
          AND EXTRACT(YEAR FROM m.measured_at) = :year
          AND EXTRACT(WEEK FROM m.measured_at) BETWEEN :isoWeekFrom AND :isoWeekTo
        GROUP BY EXTRACT(DOW FROM m.measured_at)
        ORDER BY dow
        """, nativeQuery = true)
    List<Object[]> avgByWeekday(@Param("zoneId") UUID zoneId,
                                @Param("year") int year,
                                @Param("isoWeekFrom") int isoWeekFrom,
                                @Param("isoWeekTo") int isoWeekTo);

    /**
     * Serie diaria de avgDb partida en dos por una fecha pivote.
     * Devuelve [day, avgDb, sampleCount, isBefore]. zoneId opcional.
     */
    @Query(value = """
        SELECT date_trunc('day', m.measured_at)                      AS day,
               avg(m.db_value)                                        AS avg_db,
               count(*)                                               AS sample_count,
               (m.measured_at < :pivot)                               AS is_before
        FROM measurements m
        WHERE (CAST(:zoneId AS uuid) IS NULL OR m.zone_id = CAST(:zoneId AS uuid))
        GROUP BY date_trunc('day', m.measured_at), (m.measured_at < :pivot)
        ORDER BY day
        """, nativeQuery = true)
    List<Object[]> dailyAvgBeforeAfter(@Param("zoneId") UUID zoneId,
                                       @Param("pivot") OffsetDateTime pivot);
}
