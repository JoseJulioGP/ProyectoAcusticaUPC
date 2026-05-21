package com.upc.acusticupc.sonometry.domain.model;

import com.upc.acusticupc.zones.domain.model.Zone;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "measurements",
    indexes = {
        @Index(name = "idx_measurements_zone_time", columnList = "zone_id, measured_at"),
        @Index(name = "idx_measurements_period", columnList = "period")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Measurement {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id", nullable = false)
    private Zone zone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private MeasurementBatch batch;

    @Column(name = "db_value", nullable = false)
    private Double dbValue;

    @Column(name = "measured_at", nullable = false)
    private OffsetDateTime measuredAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Period period;

    @Column(length = 10)
    private String unit;
}