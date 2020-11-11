package dev.sheldan.abstracto.statistic.emotes.command.parameter;

import dev.sheldan.abstracto.statistic.emotes.model.database.TrackedEmote;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Emote;

@Getter
@Setter
@Builder
public class TrackEmoteParameter {
    private Emote emote;
    private TrackedEmote trackedEmote;
}
