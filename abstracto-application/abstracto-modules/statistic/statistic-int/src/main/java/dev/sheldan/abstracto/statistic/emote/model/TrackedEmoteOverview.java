package dev.sheldan.abstracto.statistic.emote.model;

import dev.sheldan.abstracto.statistic.emote.model.database.TrackedEmote;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Guild;

import java.util.ArrayList;
import java.util.List;

/**
 * Model used to render the currently tracked emotes of a {@link Guild}. They are split up into
 * the static/animated emotes from the server itself. The {@link net.dv8tion.jda.api.entities.emoji.RichCustomEmoji}
 * which were previously on the server and (if enabled) all external {@link TrackedEmote} from the server.
 */
@Getter
@Setter
@Builder
public class TrackedEmoteOverview {
    /**
     * A list of {@link AvailableTrackedEmote} containing the static emotes of the {@link Guild} this command has been executed for.
     */
    @Builder.Default
    private List<AvailableTrackedEmote> animatedEmotes = new ArrayList<>();

    /**
     * A list of {@link AvailableTrackedEmote} containing the animated emotes of the {@link Guild} this command has been executed for.
     */
    @Builder.Default
    private List<AvailableTrackedEmote> staticEmotes = new ArrayList<>();

    /**
     * A list of {@link TrackedEmote} containing the static emotes which were previously in the {@link Guild}, but have since been deleted
     */
    @Builder.Default
    private List<TrackedEmote> deletedStaticEmotes = new ArrayList<>();

    /**
     * A list of {@link TrackedEmote} containing the animated emotes which were previously in the {@link Guild}, but have since been deleted
     */
    @Builder.Default
    private List<TrackedEmote> deletedAnimatedEmotes = new ArrayList<>();

    /**
     * A list of {@link TrackedEmote} containing the static emotes which were used on the {@link Guild}, but are not from that {@link Guild}.
     */
    @Builder.Default
    private List<TrackedEmote> externalStaticEmotes = new ArrayList<>();

    /**
     * A list of {@link TrackedEmote} containing the animated emotes which were used on the {@link Guild}, but are not from that {@link Guild}.
     */
    @Builder.Default
    private List<TrackedEmote> externalAnimatedEmotes = new ArrayList<>();

    /**
     * The {@link Guild} for which the {@link TrackedEmote}s have been retrieved.
     */
    private Guild guild;

    /**
     * Adds the {@link TrackedEmote} to the correct list in this model, depending on the properties of the tracked emote.
     * @param trackedEmote The {@link TrackedEmote} instance to add to the lists
     * @param guild The {@link Guild} in which trackedEmote is being tracked
     */
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
                    .emote(guild.getEmojiById(trackedEmote.getTrackedEmoteId().getId()))
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
