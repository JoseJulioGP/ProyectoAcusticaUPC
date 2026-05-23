package com.upc.acusticupc.compliance.domain.model;

import com.upc.acusticupc.sonometry.domain.model.Period;
import com.upc.acusticupc.zones.domain.model.Sector;
import com.upc.acusticupc.zones.domain.model.Subsector;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(
    name = "noise_standards",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_noise_standard",
        columnNames = {"sector", "subsector", "period", "standard_type"}
    )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoiseStandard {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private Sector sector;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private Subsector subsector;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Period period;

    @Enumerated(EnumType.STRING)
    @Column(name = "standard_type", nullable = false, length = 20)
    private StandardType standardType;

    @Column(name = "max_db", nullable = false)
    private Double maxDb;

    @Column(length = 50)
    private String regulation;
}