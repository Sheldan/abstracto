package dev.sheldan.abstracto.statistic.emote.command.parameter;

import dev.sheldan.abstracto.statistic.emote.model.database.TrackedEmote;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;

/**
 * Container class for containing both an {@link CustomEmoji} and a {@link TrackedEmote} for the purpose of a {@link dev.sheldan.abstracto.core.command.config.Parameter}.
 * This is used in {@link dev.sheldan.abstracto.statistic.emote.command.TrackEmote} and is used as a convenience parameter, in which there
 * might both a {@link CustomEmoji} and a {@link TrackedEmote} as parameter
 */
@Getter
@Setter
@Builder
public class TrackEmoteParameter {
    /**
     * If an {@link CustomEmoji} has been used as parameter, this will have the appropriate value
     */
    private CustomEmoji emote;
    /**
     * If a {@link Long} or {@link CustomEmoji} has been supplied as the parameter, this will contain a faked instance of the respective values
     */
    private TrackedEmote trackedEmote;
}
