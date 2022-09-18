package dev.sheldan.abstracto.statistic.emote.model;

import dev.sheldan.abstracto.statistic.emote.model.database.TrackedEmote;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;

/**
 * Model which is used to render in emotes for the command `showTrackedEmotes`, which still exist in the server.
 * This means there is a {@link RichCustomEmoji} instance available.
 */
@Getter
@Setter
@Builder
public class AvailableTrackedEmote {
    /**
     * The {@link RichCustomEmoji} instance of the {@link TrackedEmote} to show
     */
    private CustomEmoji emote;
    /**
     * The original {@link TrackedEmote} instance from the server to show
     */
    private TrackedEmote trackedEmote;
}
