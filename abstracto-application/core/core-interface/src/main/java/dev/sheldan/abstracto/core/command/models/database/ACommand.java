package dev.sheldan.abstracto.core.command.models.database;

import dev.sheldan.abstracto.core.models.database.AFeature;
import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "command")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Cacheable
@EqualsAndHashCode
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ACommand implements Serializable {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @Getter
    @Setter
    @JoinColumn(name = "module_id", nullable = false)
    private AModule module;

    @Getter
    @Setter
    @ManyToOne
    @JoinColumn(name = "feature_id", nullable = false)
    private AFeature feature;

    @Column(name = "created")
    private Instant created;

    @PrePersist
    private void onInsert() {
        this.created = Instant.now();
    }

    @Column(name = "updated")
    private Instant updated;

    @PreUpdate
    private void onUpdate() {
        this.updated = Instant.now();
    }

}
