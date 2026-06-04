package com.upc.acusticupc.ingestion.domain.model;

import com.upc.acusticupc.auth.domain.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Carpeta de ingesta jerárquica. Permite organizar los batches en árbol.
 * La asociación a padre es opcional (carpetas raíz tienen parent = null).
 */
@Entity
@Table(
    name = "ingestion_folders",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_folder_name_parent",
        columnNames = {"name", "parent_id"}
    )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IngestionFolder {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, length = 120)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private IngestionFolder parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
}
