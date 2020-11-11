package dev.sheldan.abstracto.statistic.emotes.model.database.embed;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.time.Instant;

@Embeddable
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class UsedEmoteDay implements Serializable {

    @Column(name = "emote_id")
    private Long emoteId;
    @Column(name = "server_id")
    private Long serverId;

    @Column(name = "use_date")
    private Instant useDate;
}
