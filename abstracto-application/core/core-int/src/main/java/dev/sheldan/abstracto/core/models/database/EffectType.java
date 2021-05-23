package dev.sheldan.abstracto.core.models.database;

import lombok.*;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "effect_type")
@Getter
@Builder
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class EffectType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "effect_type_key", nullable = false)
    private String effectTypeKey;

    @Column(name = "created", nullable = false, insertable = false, updatable = false)
    private Instant created;

    @Column(name = "updated", insertable = false, updatable = false)
    private Instant updated;
}
