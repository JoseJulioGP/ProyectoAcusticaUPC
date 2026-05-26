package com.upc.acusticupc.analytics.domain.repository;

import com.upc.acusticupc.compliance.domain.model.Alert;
import com.upc.acusticupc.compliance.domain.model.AlertSeverity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface AlertStatsRepository extends JpaRepository<Alert, UUID> {

    @Query("""
        SELECT a.severity, COUNT(a) FROM Alert a
        WHERE a.triggeredAt BETWEEN :from AND :to
        GROUP BY a.severity
        """)
    List<Object[]> countBySeverityInRange(@Param("from") OffsetDateTime from,
                                          @Param("to") OffsetDateTime to);

    @Query("""
        SELECT a.zone.name, COUNT(a) FROM Alert a
        WHERE a.triggeredAt BETWEEN :from AND :to
        GROUP BY a.zone.name
        """)
    List<Object[]> countByZoneInRange(@Param("from") OffsetDateTime from,
                                      @Param("to") OffsetDateTime to);

    @Query("""
        SELECT a FROM Alert a
        WHERE a.triggeredAt BETWEEN :from AND :to
        ORDER BY a.triggeredAt DESC
        """)
    List<Alert> findRecentInRange(@Param("from") OffsetDateTime from,
                                  @Param("to") OffsetDateTime to,
                                  org.springframework.data.domain.Pageable pageable);

    long countByTriggeredAtBetween(OffsetDateTime from, OffsetDateTime to);
}