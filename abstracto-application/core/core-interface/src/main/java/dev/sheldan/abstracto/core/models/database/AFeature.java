package dev.sheldan.abstracto.core.models.database;

import dev.sheldan.abstracto.core.command.models.database.ACommand;
import dev.sheldan.abstracto.core.models.SnowFlake;
import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="feature")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AFeature implements SnowFlake {

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
    @OneToMany
    @JoinColumn(name = "feature_id")
    private List<ACommand> commands;

}
