package dev.sheldan.abstracto.core.models.database;

import lombok.*;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "context_command")
@Getter
@Builder
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ContextCommand {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "name", length = 32)
    private String commandName;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private ContextType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feature_id", nullable = false)
    private AFeature feature;

    @Column(name = "created", nullable = false, insertable = false, updatable = false)
    private Instant created;

    @Column(name = "updated", insertable = false, updatable = false)
    private Instant updated;
}
