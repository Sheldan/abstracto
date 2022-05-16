package dev.sheldan.abstracto.statistic.emote.model;

import dev.sheldan.abstracto.statistic.emote.model.database.UsedEmote;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import java.time.Instant;
import java.util.List;

/**
 * Model used to render the file produced by `exportEmoteStats`
 */
@Getter
@Setter
@Builder
public class DownloadEmoteStatsModel {
    /**
     * The {@link Guild} for which the emote stats are being exportet
     */
    private Guild guild;
    /**
     * The date this export has been performed on
     */
    private Instant downloadDate;
    /**
     * The {@link Instant} which was used as the cut-off point in time to retrieve the emote stats for
     */
    private Instant statsSince;
    /**
     * The {@link Member} who requested the export
     */
    private Member requester;
    /**
     * A list of {@link UsedEmote} which are part of the export
     */
    private List<UsedEmote> emotes;
    private String emoteStatsFileName;
}
