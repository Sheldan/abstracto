package dev.sheldan.abstracto.core.models.database;

import dev.sheldan.abstracto.core.models.SnowFlake;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name="feature_flag")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AFeatureFlag implements SnowFlake {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    @Column(name = "id")
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @Getter
    @Setter
    @JoinColumn(name = "server_id")
    private AServer server;

    @Getter
    @Setter
    private String key;

    @Getter
    @Setter
    private boolean enabled;
}
