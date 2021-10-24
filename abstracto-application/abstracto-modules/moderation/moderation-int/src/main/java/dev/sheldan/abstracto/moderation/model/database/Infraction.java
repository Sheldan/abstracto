package dev.sheldan.abstracto.moderation.model.database;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import lombok.*;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name="infraction")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class Infraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "infraction_user_in_server_id", nullable = false)
    private AUserInAServer user;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", referencedColumnName = "id", nullable = false)
    private AServer server;

    @Column(name = "points")
    private Long points;

    @Column(name = "decayed")
    private Boolean decayed;

    @Column(name = "decayed_date")
    private Instant decayedDate;

    @Column(name = "created", nullable = false, insertable = false, updatable = false)
    private Instant created;

    @Column(name = "updated", insertable = false, updatable = false)
    private Instant updated;
}
