package dev.sheldan.abstracto.statistic.emotes.model.database.embed;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.time.Instant;

/**
 * This {@link Embeddable} is used to create the composite primary key of {@link dev.sheldan.abstracto.statistic.emotes.model.database.UsedEmote}
 * which consists of the `emote_id` (long), `server_id` (long) and the `use_date` (date)
 */
@Embeddable
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class UsedEmoteDay implements Serializable {

    /**
     * The globally unique ID of the emote
     */
    @Column(name = "emote_id")
    private Long emoteId;
    /**
     * The ID of the {@link net.dv8tion.jda.api.entities.Guild} where this emote is *tracked* in. This does not mean, that the emote originated from this server,
     * just that it is part of the tracked emotes of the server.
     */
    @Column(name = "server_id")
    private Long serverId;

    /**
     * The *day* the emote was used, this is represented as an {@link Instant} here, but in the actual database its only a date
     */
    @Column(name = "use_date")
    private Instant useDate;
}
