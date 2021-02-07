package dev.sheldan.abstracto.statistic.emotes.model.database;

import dev.sheldan.abstracto.statistic.emotes.model.database.embed.UsedEmoteDay;
import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.time.Instant;

/**
 * An actual instance which has been used on a certain date with a certain amount.
 */
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "used_emote")
@Getter
@Setter
@EqualsAndHashCode
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class UsedEmote {

    /**
     * The unique identifier for the emote: the ID of the emote, the ID of the server it was used in, the *date* the emote was used on.
     */
    @EmbeddedId
    private UsedEmoteDay emoteId;

    /**
     * Reference to the {@link TrackedEmote} which was used at the given date
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns(
    {
     @JoinColumn(updatable = false, insertable = false, name = "emote_id", referencedColumnName = "id"),
     @JoinColumn(updatable = false, insertable = false, name = "server_id", referencedColumnName = "server_id")
    })
    private TrackedEmote trackedEmote;

    /**
     * The amount this {@link TrackedEmote} has been used on this date
     */
    @Column(name = "amount")
    private Long amount;

    @Column(name = "created")
    private Instant created;

    @Column(name = "updated")
    private Instant updated;
}
