package dev.sheldan.abstracto.statistic.emotes.model;

import dev.sheldan.abstracto.statistic.emotes.model.database.TrackedEmote;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Guild;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
public class TrackedEmoteOverview {
    @Builder.Default
    private List<AvailableTrackedEmote> animatedEmotes = new ArrayList<>();

    @Builder.Default
    private List<AvailableTrackedEmote> staticEmotes = new ArrayList<>();

    @Builder.Default
    private List<TrackedEmote> deletedStaticEmotes = new ArrayList<>();

    @Builder.Default
    private List<TrackedEmote> deletedAnimatedEmotes = new ArrayList<>();

    @Builder.Default
    private List<TrackedEmote> externalStaticEmotes = new ArrayList<>();

    @Builder.Default
    private List<TrackedEmote> externalAnimatedEmotes = new ArrayList<>();

    private Guild guild;

    public void addTrackedEmote(TrackedEmote trackedEmote, Guild guild) {
        if(trackedEmote.getDeleted()) {
            if(trackedEmote.getAnimated()) {
                deletedAnimatedEmotes.add(trackedEmote);
            } else {
                deletedStaticEmotes.add(trackedEmote);
            }
        } else if(trackedEmote.getExternal()){
            if(trackedEmote.getAnimated()) {
                externalAnimatedEmotes.add(trackedEmote);
            } else {
                externalStaticEmotes.add(trackedEmote);
            }
        } else {
            AvailableTrackedEmote availableEmote = AvailableTrackedEmote
                    .builder()
                    .emote(guild.getEmoteById(trackedEmote.getTrackedEmoteId().getEmoteId()))
                    .trackedEmote(trackedEmote)
                    .build();
            if(trackedEmote.getAnimated()) {
                animatedEmotes.add(availableEmote);
            } else {
                staticEmotes.add(availableEmote);
            }
        }
    }
}
