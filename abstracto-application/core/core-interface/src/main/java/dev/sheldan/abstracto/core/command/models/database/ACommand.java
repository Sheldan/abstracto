package dev.sheldan.abstracto.core.command.models.database;

import dev.sheldan.abstracto.core.models.database.AFeature;
import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "command")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ACommand {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @Getter
    @Setter
    @JoinColumn(name = "module_id", nullable = false)
    private AModule module;

    @Getter
    @Setter
    @ManyToOne
    @JoinColumn(name = "feature_id", nullable = false)
    private AFeature feature;

}
