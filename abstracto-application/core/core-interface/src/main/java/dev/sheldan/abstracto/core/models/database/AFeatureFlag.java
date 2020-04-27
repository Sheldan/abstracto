package dev.sheldan.abstracto.core.models.database;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name="feature_flag")
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
}
