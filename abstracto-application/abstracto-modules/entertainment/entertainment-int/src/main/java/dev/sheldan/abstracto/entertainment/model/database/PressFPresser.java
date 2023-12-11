package dev.sheldan.abstracto.entertainment.model.database;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.entertainment.model.database.embed.PressFPresserId;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "press_f_presser")
@Getter
@Setter
@EqualsAndHashCode
public class PressFPresser {

    @EmbeddedId
    @Getter
    private PressFPresserId id;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @MapsId("presserId")
    @JoinColumn(name = "press_f_presser_user_in_server_id", nullable = false)
    private AUserInAServer presser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(updatable = false, insertable = false, name = "press_f_id", referencedColumnName = "id")
    private PressF pressF;

    @Column(name = "created", nullable = false, insertable = false, updatable = false)
    private Instant created;

    @Column(name = "updated", insertable = false, updatable = false)
    private Instant updated;
}
