package dev.sheldan.abstracto.core.models.database;

import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name="feature_flag")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class AFeatureFlag implements Serializable {

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
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "featureFlag")
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private List<AFeatureMode> modes;

    @Getter
    @Setter
    @Column(name = "enabled")
    private boolean enabled;

    @Column(name = "created")
    private Instant created;

    @Column(name = "updated")
    private Instant updateTimestamp;

}
