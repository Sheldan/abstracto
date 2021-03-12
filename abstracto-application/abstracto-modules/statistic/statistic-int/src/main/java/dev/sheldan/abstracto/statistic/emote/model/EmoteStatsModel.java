package dev.sheldan.abstracto.statistic.emote.model;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Guild;

import java.util.ArrayList;
import java.util.List;

/**
 * The model used to render a `*emoteStats` result. This is split up into animated and static emotes, and will only
 * represent one type of these three: still existing, deleted and external. A guild instance is added for convenience.
 */
@Getter
@Setter
@Builder
public class EmoteStatsModel {
    /**
     * The list of {@link EmoteStatsResultDisplay} which represent the animated emotes from the result
     */
    @Builder.Default
    private List<EmoteStatsResultDisplay> animatedEmotes = new ArrayList<>();
    /**
     * The list of {@link EmoteStatsResultDisplay} which represent the static emotes from the result
     */
    @Builder.Default
    private List<EmoteStatsResultDisplay> staticEmotes = new ArrayList<>();
    /**
     * The server the emote stats have been retrieved for.
     */
    private Guild guild;

    /**
     * Whether or not this model contains *any* stats to render.
     * @return Whether or not there are any stats to display
     */
    public boolean areStatsAvailable() {
        return !animatedEmotes.isEmpty() || !staticEmotes.isEmpty();
    }
}
