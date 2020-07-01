package dev.sheldan.abstracto.core.models.database;

import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name="default_feature_flag")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class DefaultFeatureFlag implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    @Column(name = "id")
    public Long id;

    @Getter
    @Setter
    @OneToOne
    @JoinColumn(name = "feature_id", nullable = false)
    private AFeature feature;

    @Getter
    @Setter
    private boolean enabled;

    @Getter
    @Setter
    private String mode;

    @Column(name = "created")
    private Instant created;

    @PrePersist
    private void onInsert() {
        this.created = Instant.now();
    }

    @Column(name = "updated")
    private Instant updateTimestamp;

    @PreUpdate
    private void onUpdate() {
        this.updateTimestamp = Instant.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultFeatureFlag that = (DefaultFeatureFlag) o;
        return enabled == that.enabled &&
                Objects.equals(id, that.id) &&
                Objects.equals(feature, that.feature) &&
                Objects.equals(mode, that.mode) &&
                Objects.equals(created, that.created) &&
                Objects.equals(updateTimestamp, that.updateTimestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, feature, enabled, mode, created, updateTimestamp);
    }
}
