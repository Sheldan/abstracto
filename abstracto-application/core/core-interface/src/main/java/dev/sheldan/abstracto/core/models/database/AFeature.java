package dev.sheldan.abstracto.core.models.database;

import dev.sheldan.abstracto.core.command.models.database.ACommand;
import dev.sheldan.abstracto.core.models.SnowFlake;
import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name="feature")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class AFeature implements SnowFlake, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    @Column(name = "id")
    public Long id;

    @Getter
    @Setter
    private String key;

    @Getter
    @Setter
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "feature")
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private List<ACommand> commands;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AFeature feature = (AFeature) o;
        return Objects.equals(id, feature.id) &&
                Objects.equals(key, feature.key) &&
                Objects.equals(commands, feature.commands);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, key, commands);
    }
}
