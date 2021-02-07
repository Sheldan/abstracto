package dev.sheldan.abstracto.core.models.database;

import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name="feature_mode")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class AFeatureMode implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    @Column(name = "id")
    public Long id;

    @Getter
    @Setter
    @ManyToOne
    @JoinColumn(name = "feature_flag_id", nullable = false)
    private AFeatureFlag featureFlag;

    @Getter
    @Setter
    @ManyToOne
    @JoinColumn(name = "feature_mode_id", nullable = false)
    private DefaultFeatureMode featureMode;

    @Getter
    @Setter
    @OneToOne
    @JoinColumn(name = "server_id", nullable = false)
    private AServer server;

    @Column(name = "enabled")
    @Getter
    @Setter
    private Boolean enabled;

    @Column(name = "created")
    private Instant created;

    @Column(name = "updated")
    private Instant updateTimestamp;

}
