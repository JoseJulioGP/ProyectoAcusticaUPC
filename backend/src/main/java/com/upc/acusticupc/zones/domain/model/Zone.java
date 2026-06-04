package com.upc.acusticupc.zones.domain.model;

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
@Table(name = "zones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Zone {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(length = 100)
    private String building;

    @Column(length = 50)
    private String floor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private Sector sector;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private Subsector subsector;

    @Column(nullable = false)
    private boolean active;

    // Posición sobre el plano (% relativo al contenedor). Nullable: zona sin posicionar.
    @Column(name = "map_x")
    private Double mapX;

    @Column(name = "map_y")
    private Double mapY;

    @Column(name = "map_width")
    private Double mapWidth;

    @Column(name = "map_height")
    private Double mapHeight;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
}