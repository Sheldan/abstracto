package dev.sheldan.abstracto.core.models.database;

import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name="feature_flag")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class AFeatureFlag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    @Column(name = "id")
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @Getter
    @Setter
    @JoinColumn(name = "server_id", nullable = false)
    private AServer server;

    @Getter
    @Setter
    @OneToOne
    @JoinColumn(name = "feature_id", nullable = false)
    private AFeature feature;

    @Getter
    @Setter
    private boolean enabled;

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
        AFeatureFlag that = (AFeatureFlag) o;
        return enabled == that.enabled &&
                Objects.equals(id, that.id) &&
                Objects.equals(server, that.server) &&
                Objects.equals(feature, that.feature);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, server, feature, enabled);
    }
}
