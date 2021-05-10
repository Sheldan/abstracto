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
    @Column(name = "id")
    private Long id;

    @Column(name = "effect_type_key")
    private String effectTypeKey;

    @Column(name = "created")
    private Instant created;

    @Column(name = "updated")
    private Instant updated;
}
