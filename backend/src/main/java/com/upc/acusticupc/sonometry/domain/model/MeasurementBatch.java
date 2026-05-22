package com.upc.acusticupc.sonometry.domain.model;

import com.upc.acusticupc.auth.domain.model.User;
import com.upc.acusticupc.zones.domain.model.Zone;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "measurement_batches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeasurementBatch {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, length = 255)
    private String fileName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id", nullable = false)
    private Zone zone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by")
    private User uploadedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BatchStatus status;

    @Column(name = "total_rows")
    private Integer totalRows;

    @Column(name = "valid_rows")
    private Integer validRows;

    @Column(name = "rejected_rows")
    private Integer rejectedRows;

    @Column(length = 1000)
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "uploaded_at", updatable = false)
    private OffsetDateTime uploadedAt;

    @Column(name = "processed_at")
    private OffsetDateTime processedAt;
}