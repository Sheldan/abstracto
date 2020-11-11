package dev.sheldan.abstracto.statistic.emotes.model;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Guild;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
public class EmoteStatsModel {
    @Builder.Default
    private List<EmoteStatsResultDisplay> animatedEmotes = new ArrayList<>();
    @Builder.Default
    private List<EmoteStatsResultDisplay> staticEmotes = new ArrayList<>();
    private Guild guild;

    public boolean areStatsAvailable() {
        return !animatedEmotes.isEmpty() || !staticEmotes.isEmpty();
    }
}
