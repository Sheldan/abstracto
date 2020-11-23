package dev.sheldan.abstracto.statistic.emotes.command.parameter;

import dev.sheldan.abstracto.statistic.emotes.model.database.TrackedEmote;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Emote;

/**
 * Container class for containing both an {@link Emote} and a {@link TrackedEmote} for the purpose of a {@link dev.sheldan.abstracto.core.command.config.Parameter}.
 * This is used in {@link dev.sheldan.abstracto.statistic.emotes.command.TrackEmote} and is used as a convenience parameter, in which there
 * might both a {@link Emote} and a {@link TrackedEmote} as parameter
 */
@Getter
@Setter
@Builder
public class TrackEmoteParameter {
    /**
     * If an {@link Emote} has been used as parameter, this will have the appropriate value
     */
    private Emote emote;
    /**
     * If a {@link Long} or {@link Emote} has been supplied as the parameter, this will contain a faked instance of the respective values
     */
    private TrackedEmote trackedEmote;
}
