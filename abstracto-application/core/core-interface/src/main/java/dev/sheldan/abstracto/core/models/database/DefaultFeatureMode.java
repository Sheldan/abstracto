package dev.sheldan.abstracto.core.models.database;

import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name="default_feature_mode")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class DefaultFeatureMode implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    @Column(name = "id")
    public Long id;

    @Getter
    @Setter
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feature_id", nullable = false)
    private AFeature feature;

    @Getter
    @Setter
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "featureMode")
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private List<AFeatureMode> modes;

    @Getter
    @Setter
    @Column(name = "enabled")
    private boolean enabled;

    @Getter
    @Setter
    @Column(name = "mode")
    private String mode;

    @Column(name = "created")
    private Instant created;

    @Column(name = "updated")
    private Instant updated;

}
