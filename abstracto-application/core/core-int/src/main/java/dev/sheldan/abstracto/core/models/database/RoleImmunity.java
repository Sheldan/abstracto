package dev.sheldan.abstracto.core.models.database;

import dev.sheldan.abstracto.core.models.RoleImmunityId;
import lombok.*;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "role_immunity")
@Getter
@Builder
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class RoleImmunity {

    @EmbeddedId
    private RoleImmunityId immunityId;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @MapsId("effectId")
    @JoinColumn(name = "effect_id", nullable = false)
    private EffectType effect;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @MapsId("roleId")
    @JoinColumn(name = "role_id", nullable = false)
    private ARole role;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="server_id", nullable = false)
    private AServer server;

    @Column(name = "created", nullable = false, insertable = false, updatable = false)
    private Instant created;

    @Column(name = "updated", insertable = false, updatable = false)
    private Instant updated;
}
