package dev.sheldan.abstracto.giveaway.model.database;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.giveaway.model.database.embed.GiveawayParticipationId;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "giveaway_participant")
@Getter
@Setter
@EqualsAndHashCode
public class GiveawayParticipant {
    @EmbeddedId
    @Getter
    private GiveawayParticipationId id;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @MapsId("participantId")
    @JoinColumn(name = "participant_user_in_server_id", nullable = false)
    private AUserInAServer participant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns(
            {
                    @JoinColumn(updatable = false, insertable = false, name = "giveaway_id", referencedColumnName = "id"),
                    @JoinColumn(updatable = false, insertable = false, name = "server_id", referencedColumnName = "server_id")
            })
    private Giveaway giveaway;

    @Column(name = "won", nullable = false)
    private Boolean won;

    @Column(name = "created", nullable = false, insertable = false, updatable = false)
    private Instant created;

    @Column(name = "updated", insertable = false, updatable = false)
    private Instant updated;

}
