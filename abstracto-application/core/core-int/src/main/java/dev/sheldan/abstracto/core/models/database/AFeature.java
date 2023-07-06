package dev.sheldan.abstracto.core.models.database;

import dev.sheldan.abstracto.core.command.model.database.ACommand;
import dev.sheldan.abstracto.core.models.SnowFlake;
import lombok.*;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name="feature")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class AFeature implements SnowFlake, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    @Column(name = "id", nullable = false)
    public Long id;

    @Getter
    @Setter
    @Column(name = "key", nullable = false)
    private String key;

    @Getter
    @Setter
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "feature")
    private List<ACommand> commands;

    @Column(name = "created", nullable = false, insertable = false, updatable = false)
    private Instant created;

    @Column(name = "updated", insertable = false, updatable = false)
    private Instant updated;

}
