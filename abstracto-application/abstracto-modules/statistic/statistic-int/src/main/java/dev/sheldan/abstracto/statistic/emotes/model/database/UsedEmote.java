package dev.sheldan.abstracto.statistic.emotes.model.database;

import dev.sheldan.abstracto.statistic.emotes.model.database.embed.UsedEmoteDay;
import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;

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

    @EmbeddedId
    private UsedEmoteDay emoteId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns(
    {
     @JoinColumn(updatable = false, insertable = false, name = "emote_id", referencedColumnName = "id"),
     @JoinColumn(updatable = false, insertable = false, name = "server_id", referencedColumnName = "server_id")
    })
    private TrackedEmote trackedEmote;

    @Column(name = "amount")
    private Long amount;
}
