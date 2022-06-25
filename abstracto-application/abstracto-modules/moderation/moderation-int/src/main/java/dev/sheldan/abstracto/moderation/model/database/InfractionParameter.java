package dev.sheldan.abstracto.moderation.model.database;

import dev.sheldan.abstracto.moderation.model.database.embedded.InfractionParameterId;
import lombok.*;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name="infraction_parameter")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class InfractionParameter {

    @EmbeddedId
    private InfractionParameterId infractionParameterId;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @MapsId("infractionId")
    @JoinColumn(name = "infraction_id", nullable = false)
    private Infraction infraction;

    @Column(name = "value")
    private String value;

    @Column(name = "created", nullable = false, insertable = false, updatable = false)
    private Instant created;

    @Column(name = "updated", insertable = false, updatable = false)
    private Instant updated;
}
