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
@Table(name = "alerts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alert {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "zone_id")
    private Zone zone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id")
    private MeasurementBatch batch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compliance_result_id")
    private ComplianceResult complianceResult;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Period period;

    @Column(name = "measured_db", nullable = false, precision = 5, scale = 2)
    private BigDecimal measuredDb;

    @Column(name = "standard_db", nullable = false, precision = 5, scale = 2)
    private BigDecimal standardDb;

    // Generated column en la BD; se mapea como insertable=false, updatable=false
    @Column(name = "excess_db", precision = 5, scale = 2, insertable = false, updatable = false)
    private BigDecimal excessDb;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertSeverity severity;

    @Column(name = "triggered_at", nullable = false)
    private OffsetDateTime triggeredAt;

    @Column(columnDefinition = "TEXT")
    private String notes;
}