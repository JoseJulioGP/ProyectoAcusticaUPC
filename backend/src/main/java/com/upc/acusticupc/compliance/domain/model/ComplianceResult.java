package com.upc.acusticupc.compliance.domain.model;

import com.upc.acusticupc.sonometry.domain.model.MeasurementBatch;
import com.upc.acusticupc.sonometry.domain.model.Period;
import com.upc.acusticupc.zones.domain.model.Zone;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "compliance_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplianceResult {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "zone_id")
    private Zone zone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id")
    private MeasurementBatch batch;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Period period;

    @Column(name = "measurement_count", nullable = false)
    private Integer measurementCount;

    @Column(name = "laeq_db", nullable = false, precision = 5, scale = 2)
    private BigDecimal laeqDb;

    @Column(name = "l90_db", nullable = false, precision = 5, scale = 2)
    private BigDecimal l90Db;

    @Column(name = "min_db", nullable = false, precision = 5, scale = 2)
    private BigDecimal minDb;

    @Column(name = "max_db", nullable = false, precision = 5, scale = 2)
    private BigDecimal maxDb;

    @Column(name = "standard_db", nullable = false, precision = 5, scale = 2)
    private BigDecimal standardDb;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ComplianceStatus status;

    @Column(name = "evaluated_from", nullable = false)
    private OffsetDateTime evaluatedFrom;

    @Column(name = "evaluated_to", nullable = false)
    private OffsetDateTime evaluatedTo;

    @Column(name = "evaluated_at", nullable = false)
    private OffsetDateTime evaluatedAt;

    @Column(name = "standard_type", nullable = false, length = 16)
    private String standardType;  // "AMBIENTAL" o "EMISION"
}
