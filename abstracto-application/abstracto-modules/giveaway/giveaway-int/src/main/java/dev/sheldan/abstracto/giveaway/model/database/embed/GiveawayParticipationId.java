package dev.sheldan.abstracto.giveaway.model.database.embed;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class GiveawayParticipationId implements Serializable {
    @Column(name = "participant_user_in_server_id")
    private Long participantId;

    @Column(name = "giveaway_id")
    private Long giveawayId;

    @Column(name = "server_id")
    private Long serverId;

}
