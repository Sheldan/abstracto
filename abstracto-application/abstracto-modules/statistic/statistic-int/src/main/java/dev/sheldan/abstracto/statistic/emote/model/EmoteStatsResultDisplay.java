package dev.sheldan.abstracto.statistic.emote.model;

import dev.sheldan.abstracto.statistic.emote.model.database.TrackedEmote;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;

/**
 * Model used to render the emote stats for various commands. This contains the concrete {@link CustomEmoji} (if available).
 * This is only the case for emotes from the server. For external and deleted emotes, only {@link EmoteStatsResultDisplay} and
 * {@link TrackedEmote} will be available.
 */
@Getter
@Setter
@Builder
public class EmoteStatsResultDisplay {
    /**
     * The actual {@link net.dv8tion.jda.api.entities.emoji.CustomEmoji} from the server, if available. Not available for deleted and external emotes.
     */
    private CustomEmoji emote;
    /**
     * The {@link EmoteStatsResult} for one particular emote, containing the amount of times the emote has been used.
     */
    private EmoteStatsResult result;
    /**
     * An instance of {@link TrackedEmote} for which this result has been retrieved.
     */
    private TrackedEmote trackedEmote;
}
